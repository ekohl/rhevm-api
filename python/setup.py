#
# This file is part of python-rhev. python-rhev is free software that is
# made available under the MIT license. Consult the file "LICENSE" that
# is distributed together with this file for the exact licensing terms.
#
# python-rhev is copyright (c) 2010 by the python-rhev authors. See the
# file "AUTHORS" for a complete overview.

import sys
import os
import os.path 
import shutil

from setuptools import setup, Command
from distutils.command.build import build
from setuptools.command.sdist import sdist
from setuptools.command.bdist_egg import bdist_egg
from distutils.errors import DistutilsError
from subprocess import Popen, PIPE

version_info = {
    'name': 'python-rhev',
    'version': '0.9-SNAPSHOT',
    'description': 'A Python API for Red Hat Enterprise Virtualization',
    'author': 'Geert Jansen',
    'author_email': 'gjansen@redhat.com',
    'url': 'http://fedorahosted.org/rhevm-api',
    'license': 'MIT',
    'classifiers': [
        'Development Status :: 4 - Beta',
        'Environment :: Console',
        'Intended Audience :: System Administrators',
        'License :: OSI Approved :: MIT License',
        'Operating System :: OS Independent',
        'Programming Language :: Python' ],
}

topdir = os.path.split(os.path.abspath(__file__))[0]
if topdir == os.getcwd():
    topdir = ''


class genschema(Command):

    user_options = []
    description = 'generate schema bindings'

    def initialize_options(self):
        pass

    def finalize_options(self):
        pass

    def run(self):
        xsd = os.path.normpath(os.path.join(topdir,
                    '..', 'api', 'src', 'main', 'resources', 'api.xsd'))
        libdir = os.path.join(topdir, 'lib', 'rhev')
        datadir = os.path.join(libdir, 'data')
        xsdcopy = os.path.join(datadir, 'api.xsd')
        schema = os.path.join(libdir, '_schema.py')
        try:
            st1 = os.stat(xsd)
        except OSError:
            st1 = None
        try:
            st2 = os.stat(xsdcopy)
        except OSError:
            st2 = None
        try:
            st3 = os.stat(schema)
        except OSError:
            st3 = None
        try:
            st4 = os.stat(datadir)
        except OSError:
            st4 = None
        if st4 is None:
            os.mkdir(datadir)
            print 'created %s' % datadir
        if st1 and (st2 is None or st1.st_mtime > st2.st_mtime):
            shutil.copy(xsd, xsdcopy)
            print 'copied %s => %s' % (xsd, xsdcopy)
            st2 = os.stat(xsdcopy)
        if st1 is None and st2 is None:
            raise DistutilsError, 'Could not find schema'
        if st3 and st2.st_mtime < st3.st_mtime:
            print 'schema is up to date'
            return
        try:
            pipe = Popen(('pyxbgen', '-m', '_schema', '--binding-root',
                          libdir, xsdcopy), stdout=PIPE, stderr=PIPE)
            pipe.communicate()
        except OSError:
            raise DistutilsError, 'Could not find pyxbgen.'
        if pipe.returncode:
            raise DistutilsError, 'Could not generate schema'
        print 'xmlschema input: %s' % xsdcopy
        print 'schema generated as: %s' % schema


class mybuild(build):

    def run(self):
        self.run_command('genschema')
        build.run(self)


class mysdist(sdist):

    def run(self):
        self.run_command('genschema')
        sdist.run(self)


class mybdist_egg(bdist_egg):

    def run(self):
        self.run_command('genschema')
        bdist_egg.run(self)


setup(
    packages = [ 'rhev', 'rhev.test' ],
    package_dir = { '': os.path.join(topdir, 'lib') },
    package_data = { 'rhev': [ os.path.join('data', 'api.xsd') ] },
    test_suite = 'nose.collector',
    entry_points = {
        'nose.plugins.0.10': [ 'deploader = rhev.test.loader:DepLoader' ]
    },
    cmdclass = { 'build': mybuild, 'sdist': mysdist,
                 'bdist_egg': mybdist_egg, 'genschema': genschema },
    **version_info
)
