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
package com.redhat.rhevm.api.common.util;

import org.junit.Assert;
import org.junit.Test;

import com.redhat.rhevm.api.model.Attachment;
import com.redhat.rhevm.api.model.Attachments;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VM;


public class LinkHelperTest extends Assert {

    private static final String VM_ID = "awesome";
    private static final String CLUSTER_ID = "alarming";
    private static final String TEMPLATE_ID = "astonishing";
    private static final String VM_POOL_ID = "beautiful";
    private static final String STORAGE_DOMAIN_ID = "breathtaking";
    private static final String HOST_ID = "magnificent";
    private static final String DATA_CENTER_ID = "majestic";
    private static final String NETWORK_ID = "stupendous";

    private static final String VM_HREF = "vms/" + VM_ID;
    private static final String CLUSTER_HREF = "clusters/" + CLUSTER_ID;
    private static final String TEMPLATE_HREF = "templates/" + TEMPLATE_ID;
    private static final String VM_POOL_HREF = "vmpools/" + VM_POOL_ID;
    private static final String STORAGE_DOMAIN_HREF = "storagedomains/" + STORAGE_DOMAIN_ID;
    private static final String HOST_HREF = "hosts/" + HOST_ID;
    private static final String DATA_CENTER_HREF = "datacenters/" + DATA_CENTER_ID;
    private static final String NETWORK_HREF = "networks/" + NETWORK_ID;
    private static final String ATTACHMENT_HREF = STORAGE_DOMAIN_HREF + "/attachments/" + DATA_CENTER_ID;

    @Test
    public void testVmLinks() throws Exception {
        VM vm = new VM();
        vm.setId(VM_ID);
        vm.setCluster(new Cluster());
        vm.getCluster().setId(CLUSTER_ID);
        vm.setTemplate(new Template());
        vm.getTemplate().setId(TEMPLATE_ID);
        vm.setVmPool(new VmPool());
        vm.getVmPool().setId(VM_POOL_ID);

        LinkHelper.addLinks(vm);

        assertEquals(vm.getHref(), VM_HREF);
        assertEquals(vm.getCluster().getHref(), CLUSTER_HREF);
        assertEquals(vm.getTemplate().getHref(), TEMPLATE_HREF);
        assertEquals(vm.getVmPool().getHref(), VM_POOL_HREF);
    }

    @Test
    public void testClusterLinks() throws Exception {
        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        cluster.setDataCenter(new DataCenter());
        cluster.getDataCenter().setId(DATA_CENTER_ID);

        LinkHelper.addLinks(cluster);

        assertEquals(cluster.getHref(), CLUSTER_HREF);
        assertEquals(cluster.getDataCenter().getHref(), DATA_CENTER_HREF);
    }

    @Test
    public void testHostLinks() throws Exception {
        Host host = new Host();
        host.setId(HOST_ID);

        LinkHelper.addLinks(host);

        assertEquals(host.getHref(), HOST_HREF);
    }

    @Test
    public void testStorageDomainLinks() throws Exception {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(STORAGE_DOMAIN_ID);

        LinkHelper.addLinks(storageDomain);

        assertEquals(storageDomain.getHref(), STORAGE_DOMAIN_HREF);
    }

    @Test
    public void testDataCenterLinks() throws Exception {
        DataCenter dataCenter = new DataCenter();
        dataCenter.setId(DATA_CENTER_ID);

        LinkHelper.addLinks(dataCenter);

        assertEquals(dataCenter.getHref(), DATA_CENTER_HREF);
    }

    @Test
    public void testNetworkLinks() throws Exception {
        Network network = new Network();
        network.setId(NETWORK_ID);

        LinkHelper.addLinks(network);

        assertEquals(network.getHref(), NETWORK_HREF);
    }

    @Test
    public void testAttachmentLinks() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId(DATA_CENTER_ID);
        attachment.setStorageDomain(new StorageDomain());
        attachment.getStorageDomain().setId(STORAGE_DOMAIN_ID);

        LinkHelper.addLinks(attachment);

        assertEquals(attachment.getHref(), ATTACHMENT_HREF);
        assertEquals(attachment.getStorageDomain().getHref(), STORAGE_DOMAIN_HREF);
    }

    @Test
    public void testAttachmentWithoutStorageDomain() throws Exception {
        Attachment attachment = new Attachment();
        attachment.setId(DATA_CENTER_ID);

        LinkHelper.addLinks(attachment);

        assertNull(attachment.getHref());
    }
}