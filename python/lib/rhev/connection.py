#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import re
import time
import fnmatch
import logging
import socket
import httplib as http

from httplib import HTTPConnection, HTTPSConnection
from httplib import HTTPException
from urllib import quote as urlencode
from rhev import schema, compat
from rhev.error import *


class Connection(object):
    """Access the RHEV API."""

    retries = 5
    verbosity = 1
    timeout = 300

    def __init__(self, url=None, username=None, password=None):
        """Constructor."""
        self.url = url
        self.username = username
        self.password = password
        self.scheme = None
        self.entrypoint = None
        self._logger = logging.getLogger('rhev.Connection')
        self._connection = None
        self._api = None
        self._capabilities = None

    def _parse_url(self):
        """INTERNAL: Parse an URL into its components."""
        if self.url is None:
            raise Error, 'URL not set'
        parsed = compat.urlparse(self.url)
        if not parsed.scheme:
            raise Error, 'Invalid URL with no scheme: %s' % self.url
        elif parsed.scheme not in ('http', 'https'):
            raise Error, 'Invalid URL with unknown scheme: %s' % self.url
        elif not parsed.path:
            raise Error, 'Invalid URL with no path: %s' % self.url
        if parsed.netloc.count(':') == 1:
            host, port = parsed.netloc.split(':')
            port = int(port)
        elif parsed.scheme == 'http':
            host = parsed.netloc
            port = http.HTTP_PORT
        elif parsed.scheme == 'https':
            host = parsed.netloc
            port = http.HTTPS_PORT
        self.scheme = parsed.scheme
        self.host = host
        self.port = port
        self.entrypoint = parsed.path

    def connect(self, url=None, username=None, password=None):
        """Connect to the REST API. It is safe to call this method multiple
        times, only one connection will be made. It is not required to call
        this method. It will be called automatically when a connection is
        required."""
        if self._connection is not None:
            return
        if url is not None:
            self.url = url
        if username is not None:
            self.username = username
        if password is not None:
            self.password = password
        self._parse_url()
        self._connect()

    def _connect(self):
        """INTERNAL: Connect to the REST API."""
        if self.username is None or self.password is None:
            raise Error, 'RHEV username/password not set.'
        if self.username.count('@') != 1:
            raise Error, 'Username must be in name@domain format.'
        if self.scheme == 'https':
            factory = HTTPSConnection
        elif self.scheme == 'http':
            factory = HTTPConnection
        self._connection = factory(self.host, self.port)
        self._logger.debug('connecting to RHEV-M at %s:%s' % (self.host, self.port))
        try:
            self._connection.connect()
        except socket.error, e:
            raise ConnectionError, str(e)

    def _close(self):
        """INTERNAL: Close the connection."""
        try:
            self._connection.close()
        except socket.error:
            pass
        self._connection = None
        self._logger.debug('disconnected from RHEV-M')

    def close(self):
        """Close the connection to the API."""
        if self._connection is None:
            return
        self._close()
        self._api = None
        self._capabilities = None

    def _do_request_retry(self, method, url, headers, body):
        """INTERNAL: make a HTTP request, and retry if necessary."""
        for i in range(self.retries):
            try:
                self._connection.request(method, url, body, headers)
                response = self._connection.getresponse()
                response.body = response.read()
            except (socket.error, HTTPException), e:
                self._logger.debug('Could not complete request, retry %d/%d' %
                                   (i+1, self.retries))
                self._close()
                self._connect()
            else:
                break
        else:
            m = 'Could not complete request after %s retries.' % self.retries
            raise ConnectionError, m
        return response

    def _do_request(self, method, url, headers, body):
        """INTERNAL: perform a HTTP request."""
        ctype = headers['Content-Type']
        self._logger.debug('making request: %s %s (%s; %d bytes)'
                           % (method, url, ctype, len(body)))
        if self.verbosity > 5:
            self._logger.debug('request body: %s' % body)
        response = self._do_request_retry(method, url, headers, body)
        response.body = response.body.decode('utf-8')
        clen = response.getheader('Content-Length')
        if clen:
            length = '%s bytes' % clen
        else:
            length = response.getheader('Transfer-Encoding', 'unknown')
        ctype = response.getheader('Content-Type')
        self._logger.debug('got response: %s %s (%s; %s bytes)'
                           % (response.status, response.reason, ctype, clen))
        if self.verbosity > 5:
            self._logger.debug('response body: %s' % response.body)
        return response

    def _get_headers(self):
        """INTERNAL: get the default HTTP headers."""
        headers = {}
        authval = '%s:%s' % (self.username, self.password)
        authval = authval.encode('base64').rstrip()
        headers['Authorization'] = 'Basic %s' % authval
        headers['Accept'] = 'application/xml'
        headers['Content-Type'] = 'application/xml; charset=utf-8'
        headers['Accept-Charset'] = 'utf-8'
        return headers

    def _make_request(self, method, url, headers=None, body=None):
        """INTERNAL: perform a HTTP request to the API, following redirects."""
        if headers is None:
            headers = self._get_headers()
        if body is None:
            body = ''
        elif isinstance(body, unicode):
            body = body.encode('utf-8')
        for i in range(3):
            response = self._do_request(method, url, headers, body)
            if response.status != http.FOUND:
                break
            url = response.getheader('Location')
            url = compat.urlparse(url).path
        return response

    def _fixup_object(self, obj):
        """INTERNAL: fixup a mapping object."""
        # Here we put stuff that we'd like to become part of the API itself.
        if isinstance(obj, schema.API):
            obj.href = self.entrypoint
        if hasattr(obj, 'link'):
            for link in obj.link:
                info = schema.type_info(link.rel, base=obj)
                if info:
                    link.type = info[0].__name__
        if hasattr(obj, 'actions') and hasattr(obj.actions, 'link'):
            for link in obj.actions.link:
                link.type = 'Action'
        return obj

    def _parse_xml(self, response):
        """INTERNAL: parse an XML response"""
        ctype = response.getheader('Content-Type')
        body = response.body
        if ctype not in ('application/xml', 'text/xml'):
            reason = 'Expecting an XML response (got: %s)' % ctype
            raise ResponseError(reason, detail=body)
        try:
            obj = schema.create_from_xml(body)
        except Exception, e:
            reason = 'Could not parse XML response: %s' % str(e)
            raise ResponseError(reason, detail=body)
        self._fixup_object(obj)
        obj._connection = self
        return obj

    def _join_path(self, *segments):
        path = []
        for seg in segments:
            path += [ s for s in seg.split('/') if s ]
        joined = '/' + '/'.join(path)
        return joined

    def _resolve_url(self, typ, base=None, id=None, search=None,
                     special=None, **query):
        """INTERNAL: resolve a relationship `name' under the URL `base'."""
        info = schema.type_info(typ)
        if info is None:
            raise TypeError, 'Unknown binding type: %s' % typ
        rel = info[3]
        if rel is None:
            raise TypeError, 'Can\'t resolve URLs for type: %s' % typ
        elif rel.startswith('/'):
            return self._join_path(self.entrypoint, rel)
        if base is None:
            base = self.api()
        if search:
            special = 'search'
        elif query:
            special = 'search'
            search = ' AND '.join([ '%s=%s' % (k, query[k]) for k in query ])
        if special:
            rel = '%s/%s' % (rel, special)
        for link in base.link:
            if link.rel == rel:
                url = link.href
                break
        else:
            reason = 'relationship %s not found under %s' % (rel, base.href)
            raise Error(reason)
        if id is not None:
            url = self._join_path(url, str(id))
        elif search is not None:
            url = url.replace('{query}', urlencode(search))
        return url

    def _resolve_action(self, url, action):
        """INTERNAL: Return the URL for the action beloning to the
        resource."""
        response = self._make_request('GET', url)
        if response.status != http.OK:
            raise self._create_exception(response)
        resource = self._parse_xml(response)
        if resource.actions:
            for link in resource.actions.link:
                if link.rel == action:
                    return link.href
        raise IllegalAction, 'No such action: %s' % action

    def _create_exception(self, response):
        """INTERNAL: Create an exception with some useful details."""
        ctype = response.getheader('Content-Type')
        if ctype == 'application/xml':
            body = self._parse_xml(response)
        else:
            body = None
        kwargs = {}
        if response.status == http.NOT_FOUND:
            error = NotFound
            message = 'The collection or resource was not found'
        elif response.status == http.METHOD_NOT_ALLOWED:
            error = IllegalMethod
            message = 'The method is not allowed on the collection or resource'
            kwargs['detail'] = 'Allowed methods: %s' % response.getheader('Allow')
        elif body and isinstance(body, schema.Fault):
            error = Fault
            message = body.reason
            kwargs['detail'] = body.detail
        else:
            error = ResponseError
            message = 'Unexpected HTTP status code: %s' % response.status
            kwargs['status'] = response.status
            kwargs['detail'] = response.reason
        exception = error(message, **kwargs)
        return exception

    def _filter_results(self, result, filter):
        """INTERNAL: filter a set of results."""
        matched = []; compiled = []
        for attr in filter:
            match = filter[attr]
            if isinstance(match, str):
                match = re.compile(fnmatch.translate(match))
            compiled.append((attr.split('.'), match))
        for res in result:
            for attr, match in compiled:
                value = res
                for at in attr:
                    value = getattr(value, at, None)
                    if value is None:
                        break
                if not hasattr(match, 'match') and value != match:
                    break
                if hasattr(match, 'match') and (value is None \
                                or not match.match(str(value))):
                    break
            else:
                matched.append(res)
        compat.set_slice(result, matched)
        return result

    def ping(self):
        """Ping the API, to make sure we have a proper connection."""
        self.connect()
        response = self._make_request('GET', self.entrypoint)
        if response.status != http.OK:
            raise self._create_exception(response)
        reply = self._parse_xml(response)
        if not isinstance(reply, schema.API):
            m = 'Expecting an <api> element at the API entry point.'
            raise ResponseError, m

    def getall(self, typ, base=None, search=None, filter=None, special=None,
               detail=None, **query):
        """Get a list of resources that match the supplied arguments. In
        case of success a binding instance is returned. In case of failure
        a Error is raised."""
        self.connect()
        url = self._resolve_url(typ, base=base, special=special,
                                search=search, **query)
        headers = self._get_headers()
        if detail is not None:
            headers['Accept'] += '; detail=%s' % detail
        response = self._make_request('GET', url, headers)
        if response.status == http.OK:
            result = self._parse_xml(response)
            if filter:
                result = self._filter_results(result, filter)
        elif response.status == http.NOT_FOUND:
            result = []
        else:
            raise self._create_exception(response)
        return result

    def get(self, typ, id=None, base=None, special=None, search=None,
            filter=None, detail=None, **query):
        """Get a single resource that matches the supplied arguments.In
        case of success a binding instance is returned. In case of failure a
        Error is raised."""
        self.connect()
        url = self._resolve_url(typ, base=base, id=id, search=search,
                                special=special, **query)
        headers = self._get_headers()
        if detail is not None:
            headers['Accept'] += '; detail=%s' % detail
        response = self._make_request('GET', url, headers)
        if response.status == http.OK:
            result = self._parse_xml(response)
            if not isinstance(result, schema.BaseResources):
                result = [result]
            if filter:
                result = self._filter_results(result, filter)
            if not issubclass(typ, schema.BaseResources):
                if len(result):
                    result = result[0]
                else:
                    result = None
        elif response.status == http.NOT_FOUND:
            result = None
        else:
            # Trac ticket #121: INTERNAL_SERVER_ERROR should be NOT_FOUND
            raise self._create_exception(response)
        return result

    def reload(self, resource, detail=None):
        """Reload an existing resource."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding object.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding object.'
        self.connect()
        headers = self._get_headers()
        if detail is not None:
            headers['Accept'] += '; detail=%s' % detail
        response = self._make_request('GET', resource.href, headers)
        if response.status == http.OK:
            result = self._parse_xml(response)
        elif response.status == http.NOT_FOUND:
            result = None
        else:
            raise self._create_exception(response)
        return result

    def create(self, resource, base=None):
        """Create a new resource. If base is provided, it must be a
        resource, and the new resource is created subordinate to it.  On
        success, a new instance is returned corresponding to the newly
        created resource."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if resource.href and base is None:
            raise ValueError, 'Expecting a new binding instance.'
        self.connect()
        url = self._resolve_url(type(resource), base)
        response = self._make_request('POST', url, body=resource.toxml())
        if response.status in (http.OK, http.CREATED, http.ACCEPTED):
            result = self._parse_xml(response)
        else:
            raise self._create_exception(response)
        return result

    def update(self, resource):
        """Update a resource. On success a new instance is returned
        corresponding to the updated values."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        self.connect()
        response = self._make_request('PUT', resource.href, body=resource.toxml())
        if response.status == http.OK:
            result = self._parse_xml(response)
        else:
            raise self._create_exception(response)
        return result

    def delete(self, resource, base=None, data=None):
        """Delete a resource."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        self.connect()
        if base is None:
            url = resource.href
        else:
            url = self._resolve_url(type(resource), base=base,
                                    id=resource.id)
        if data is not None:
            data = data.toxml()
        response = self._make_request('DELETE', url, body=data)
        if response.status in (http.OK, http.NO_CONTENT):
            result = None
        else:
            raise self._create_exception(response)
        return result

    def action(self, resource, action, data=None):
        """Execute an action on a resource. """
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        self.connect()
        url = self._resolve_action(resource.href, action)
        if data is None:
            data = schema.new(schema.Action)
        response = self._make_request('POST', url, body=data.toxml())
        if response.status in (http.OK, http.NO_CONTENT):
            result = self._parse_xml(response)
        else:
            raise self._create_exception(response)
        return result

    def api(self):
        """Return the API entry point."""
        if self._api is None:
            self._api = self.get(schema.API)
        return self._api

    def capabilities(self):
        """Return the capabilties."""
        if self._capabilties is None:
            self._capabilties = self.get(schema.Capabilities)
        return self._capabilities

    def wait(self, resource, timeout=None, exists=None, **conditions):
        """Wait for a resource to enter into a certain state. Extra keyword
        arguments that are provided specify the condition to wait for. The
        argument name specifies an attribute and the value the value to wait
        for."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        if timeout is None:
            timeout = self.timeout
        delay = 1
        match = False
        start = time.time()
        while time.time() < start + timeout:
            resource = self.reload(resource)
            if resource is None and exists is False or \
                    resource is not None and exists is True:
                match = True
                break
            for attr in conditions:
                if getattr(resource, attr) != conditions[attr]:
                    break
            else:
                match = True
                break
            time.sleep(delay)
            delay = min(10, 2*delay)
        return match

    def get_methods(self, obj, base=None):
        """Return the methods that are available for `resource'."""
        if isinstance(obj, schema.BaseResource):
            if not obj.href:
                raise ValueError, 'Expecting a created binding instance.'
            url = obj.href
        elif issubclass(obj, schema.BaseResources):
            url = self._resolve_url(obj, base=base)
        else:
            m = 'Expecting BaseResource instance or BaseResources subclass.'
            raise TypeError, m
        methods = []
        response = self._make_request('OPTIONS', url)
        if response.status != http.OK:
            raise self._create_exception(response)
        methods = response.getheader('Allow').split(',')
        methods = [ method.strip() for method in methods ]
        return methods

    def get_actions(self, resource):
        """Return the actions for a resource."""
        if not isinstance(resource, schema.BaseResource):
            raise TypeError, 'Expecting a binding instance.'
        if not resource.href:
            raise ValueError, 'Expecting a created binding instance.'
        actions = []
        if resource.actions:
            for link in resource.actions.link:
                actions.append(link.rel)
        return actions

    def get_links(self, obj):
        """Return the subcollections of a resource."""
        if not isinstance(obj, schema.BaseResource):
            raise TypeError, 'Expecting a BaseResource instance.'
        links = []
        for link in obj.link:
            links.append(link.rel)
        return links
