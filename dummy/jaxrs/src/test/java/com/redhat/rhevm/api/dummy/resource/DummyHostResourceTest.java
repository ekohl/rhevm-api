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
package com.redhat.rhevm.api.dummy.resource;

import org.jboss.resteasy.client.ClientResponseFailure;
import org.junit.Test;

import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Hosts;

public class DummyHostResourceTest extends DummyTestBase {
    private DummyTestBase.HostsResource getService() {
        return createHostsResource(getEntryPoint("hosts").getHref());
    }

    private void checkHost(Host host) {
        assertNotNull(host.getName());
        assertNotNull(host.getId());
        assertNotNull(host.getHref());
        assertTrue(host.getHref().endsWith("/hosts/" + host.getId()));
        assertNotNull(host.getActions());
        assertTrue(host.getActions().getLinks().size() > 0);
        boolean includesApproveLink = false;
        for (Link actionLink : host.getActions().getLinks()) {
            includesApproveLink = actionLink.getHref().endsWith("/hosts/" + host.getId() + "/approve");
            if (includesApproveLink) {
                break;
            }
        }
        assertTrue("expected approe link", includesApproveLink);
    }

    @Test
    public void testGetHostsList() throws Exception {
        DummyTestBase.HostsResource service = getService();
        assertNotNull(service);

        Hosts hosts = service.list();
        assertNotNull(hosts);
        assertTrue(hosts.getHosts().size() > 0);

        for (Host host : hosts.getHosts()) {
            checkHost(host);

            Host t = service.get(host.getId());
            checkHost(t);
            assertEquals(host.getId(), t.getId());
        }
    }

    @Test
    public void testHostGoodUpdate() throws Exception {
        DummyTestBase.HostsResource service = getService();
        assertNotNull(service);

        Host update = new Host();
        update.setName("wonga");
        Host updated = service.update("1", update);
        assertNotNull(updated);
        assertEquals(updated.getName(), "wonga");
        checkHost(updated);
    }

    @Test
    public void testHostBadUpdate() throws Exception {
        DummyTestBase.HostsResource service = getService();
        assertNotNull(service);

        Host update = new Host();
        update.setId("fluffy");
        try {
            service.update("2", update);
            fail("expected ClientResponseFailure");
        } catch (ClientResponseFailure cfe) {
            assertEquals(409, cfe.getResponse().getStatus());
            Fault fault = (Fault)cfe.getResponse().getEntity(Fault.class);
            assertNotNull(fault);
            assertEquals("Broken immutability constraint", fault.getReason());
            assertEquals("Attempt to set immutable field: id", fault.getDetail());
        }
    }
}
