#
# This file is part of rhevsh. rhevsh is free software that is made
# available under the MIT license. Consult the file "LICENSE" that is
# distributed together with this file for the exact licensing terms.
#
# rhevsh is copyright (c) 2011 by the rhevsh authors. See the file
# "AUTHORS" for a complete overview.

import os
import os.path
import sys

stdin_fileno = sys.stdin.fileno()
stdout_fileno = sys.stdout.fileno()
stderr_fileno = sys.stderr.fileno()


def which(cmd):
    """Find a command `cmd' in the path."""
    if cmd.startswith('/') and os.access(cmd, os.X_OK):
        return cmd
    path = os.environ.get('PATH')
    path = path.split(os.pathsep)
    for dir in path:
        fname = os.path.join(dir, cmd)
        if os.access(fname, os.X_OK):
            return fname


def spawn(cmd, args, debug=False):
    """Spawn a command. Return a (pid, stdin). If debug is False, stdout and
    stderr will be redirected to /dev/null."""
    stdin, pstdin = os.pipe()
    pid = os.fork()
    if pid > 0:
        os.close(stdin)
        return (pid, pstdin)
    # Put ourselves in a new session so that we don't get any signal related
    # to the terminal that rhevsh is running in.
    os.setsid()
    os.close(pstdin)
    if stdin != stdin_fileno:
        os.close(stdin_fileno)
        os.dup2(stdin, stdin_fileno)
    if not debug:
        os.close(stdout_fileno)
        stdout = os.open('/dev/null', os.O_WRONLY)
        if stdout != stdout_fileno:
            os.dup2(stdout, stdout_fileno)
            os.close(stdout)
        os.close(stderr_fileno)
        stderr = os.open('/dev/null', os.O_WRONLY)
        if stderr != stderr_fileno:
            os.dup2(stderr, stderr_fileno)
            os.close(stderr)
    os.execv(cmd, args)
    # exec failed.. the error message only gets through if debug=True
    os.write(stderr_fileno, 'execv() failed')
    sys.exit(1)


def get_home_dir():
    """Return the user's home directory."""
    home = os.environ.get('HOME')
    if home is not None:
        return home
    try:
        pw = pwd.getpwuid(os.getuid())
    except Keyerror:
        return None
    return pw.pw_dir
