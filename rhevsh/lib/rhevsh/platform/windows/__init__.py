#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import sys

if sys.platform in ('linux2',):
    from rhevsh.platform.posix import util
    from rhevsh.platform.posix import vnc
    from rhevsh.platform.posix import spice

elif sys.platform in ('win32',):
    pass
