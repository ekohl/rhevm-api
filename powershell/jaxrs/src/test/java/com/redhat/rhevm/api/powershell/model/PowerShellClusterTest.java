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
package com.redhat.rhevm.api.powershell.model;

import org.junit.Test;

import java.util.List;

import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.SchedulingPolicyType;

public class PowerShellClusterTest extends PowerShellModelTest {

    private void testCluster(Cluster c, String id, String name, String description, String cpuName, String dataCenterId, int major, int minor, int overcommit, SchedulingPolicyType schedPolicy, int high, int duration) {
        assertEquals(c.getId(), id);
        assertEquals(c.getName(), name);
        assertEquals(c.getDescription(), description);
        assertNotNull(c.getCpu());
        assertEquals(c.getCpu().getId(), cpuName);
        assertNotNull(c.getDataCenter());
        assertEquals(c.getDataCenter().getId(), dataCenterId);
        assertNotNull(c.getVersion());
        assertEquals(major, c.getVersion().getMajor());
        assertEquals(minor, c.getVersion().getMinor());
        assertNotNull(c.getMemoryPolicy());
        assertNotNull(c.getMemoryPolicy().getOverCommit());
        assertEquals(overcommit, c.getMemoryPolicy().getOverCommit().getPercent());
        if (schedPolicy != null) {
            assertNotNull(c.getSchedulingPolicy());
            assertEquals(schedPolicy.value(), c.getSchedulingPolicy().getPolicy());
            assertNotNull(c.getSchedulingPolicy().getThresholds());
            assertFalse(c.getSchedulingPolicy().getThresholds().isSetLow());
            assertEquals(high, c.getSchedulingPolicy().getThresholds().getHigh());
            assertEquals(duration, c.getSchedulingPolicy().getThresholds().getDuration());
        } else {
            assertNull(c.getSchedulingPolicy());
        }
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("cluster.xml");
        assertNotNull(data);

        List<Cluster> clusters = PowerShellCluster.parse(getParser(), data);

        assertEquals(clusters.size(), 1);

        testCluster(clusters.get(0), "0", "Default", "The default server cluster", "Intel Xeon Core i7", "fbec66b1-a219-4e0f-8023-385ca86c5707", 2, 2, 200, SchedulingPolicyType.EVENLY_DISTRIBUTED, 75, 120);
    }
}
