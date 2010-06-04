#!/usr/bin/env python

# Copyright (C) 2010 Red Hat, Inc.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

import http
import xmlfmt
import yamlfmt
import jsonfmt
import sys
import getopt
import time
from testutils import *

opts = parseOptions()

(name, address, path, host) = (None, None, None, None)
if len(opts['oargs']) >= 3:
   (name, address, path, host) = opts['oargs'][0:4]

links = http.HEAD_for_links(opts)

def find_host(t, name):
   hosts = filter(lambda h: h.name == name,
                  t.get(links['hosts'], t.fmt.parse))
   if len(hosts) == 0:
      raise RuntimeError("host '%s' not found" % name)
   return hosts[0]

def find_data_center(t, name):
   datacenters = filter(lambda d: d.name == name,
                        t.get(links['datacenters'], t.fmt.parse))
   if len(datacenters) == 0:
      raise RuntimeError("data center '%s' not found" % name)
   return datacenters[0]

for fmt in [xmlfmt]:
    t = TestUtils(opts, fmt)

    print "=== ", fmt.MEDIA_TYPE, " ==="

    for dom in t.get(links['storagedomains']):
        t.get(dom.href)
        for att in t.get(dom.link['attachments'].href):
            t.get(att.href)

    if name is None:
        continue

    dom = fmt.StorageDomain()
    dom.name = name
    dom.type = 'DATA'
    dom.storage = fmt.Storage()
    dom.storage.type = 'NFS'
    dom.storage.address = address
    dom.storage.path = path
    dom = t.create(links['storagedomains'], dom)

    h = fmt.Host()
    h.id = find_host(t, host).id

    t.syncAction(dom.actions, "initialize", host=h)

    attachment = fmt.Attachment()
    attachment.data_center = fmt.DataCenter()
    attachment.data_center.id = find_data_center(t, 'Default').id
    attachment = t.create(dom.link['attachments'].href, attachment)

    t.syncAction(attachment.actions, "activate")

    attachment = t.get(attachment.href)

    t.syncAction(attachment.actions, "deactivate")

    t.delete(attachment.href)

    dom = t.get(dom.href)

    t.syncAction(dom.actions, "teardown", host=h)

    t.delete(dom.href)