/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.powershell.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import com.redhat.rhevm.api.common.invocation.Current;
import com.redhat.rhevm.api.common.security.auth.Principal;

public class PowerShellPool {

    private static final int DEFAULT_SIZE_LOW = 2;
    private static final int DEFAULT_SIZE_HIGH = 6;
    private static final long CLEAN_INTERVAL = 300 * 1000;

    private ExecutorService executor;
    private Principal principal;
    private Current current;

    private BlockingQueue<PowerShellCmd> cmds = new LinkedBlockingQueue<PowerShellCmd>();

    private int lowSize;
    private int highSize;
    private int spawned;

    public PowerShellPool(ExecutorService executor, Principal principal, Current current, int lowSize, int highSize, long cleanInterval) {
        this.executor = executor;
        this.principal = principal;
        this.current = current;
        this.lowSize = lowSize;
        this.highSize = highSize;

        for (int i = 0; i < lowSize; i++) {
            spawn();
        }

        Timer timer = new Timer();
        timer.schedule(new PowerShellPoolCleaner(), cleanInterval, cleanInterval);
    }

    public PowerShellPool(ExecutorService executor, Principal principal, Current current) {
        this(executor, principal, current, DEFAULT_SIZE_LOW, DEFAULT_SIZE_HIGH, CLEAN_INTERVAL);
    }

    private void spawn() {
        executor.execute(new PowerShellLauncher());
        ++spawned;
    }

    public PowerShellCmd get() {
        PowerShellCmd cmd;
        while (true) {
            try {
                cmd = cmds.take();
                break;
            } catch (InterruptedException ex) {
                // ignore and block again
            }
        }
        if (cmds.size() < lowSize && spawned < highSize) {
            spawn();
        }
        return cmd;
    }

    public void add(PowerShellCmd cmd) {
        cmds.offer(cmd);
    }

    public void cleanEmpty() {
        PowerShellCmd cmd;
        while (spawned > lowSize && cmds.size() > lowSize) {
            cmd = cmds.poll();
            if (cmd != null) {
                cmd.stop();
                spawned--;
            }
        }
    }

    public void shutdown() {
        executor.shutdown();

        PowerShellCmd cmd;
        while ((cmd = cmds.poll()) != null) {
            cmd.stop();
        }
    }

    private class PowerShellPoolCleaner extends TimerTask {
        @Override
        public void run() {
            cleanEmpty();
        }
    }

    private class PowerShellLauncher implements Runnable {
        @Override public void run() {
            PowerShellCmd cmd = new PowerShellCmd(principal, current);
            cmd.start();
            PowerShellPool.this.add(cmd);
        }
    }
}
