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
package com.redhat.rhevm.api.powershell.resource;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.redhat.rhevm.api.common.util.DetailHelper.Detail;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.enums.PowerShellBootSequence;
import com.redhat.rhevm.api.powershell.resource.PowerShellVmsResource.Method;

import org.junit.Test;

public class PowerShellVmsResourceTest extends AbstractPowerShellCollectionResourceTest<VM, PowerShellVmResource, PowerShellVmsResource> {

    public static String TEMPLATE_ID = "template1";
    public static String TEMPLATE_NAME = "cookiecutter";
    public static String CLUSTER_ID = "1";
    public static String CLUSTER_NAME = "pleiades";

    public static int MEMSIZE = 1024;
    public static int BOOTSEQUENCE = PowerShellBootSequence.CDN.getValue();
    public static int N_SOCKETS = 2;
    public static int N_CPUS = 4;
    public static String ISO_NAME = "foo.iso";
    public static String HOST_ID = "2";
    public static String POOL_ID = "3";

    public static final String[] extraArgs = new String[] { CLUSTER_ID, TEMPLATE_ID, Integer.toString(MEMSIZE), Integer.toString(BOOTSEQUENCE), Integer.toString(N_SOCKETS), Integer.toString(N_CPUS), ISO_NAME, HOST_ID, POOL_ID };

    public static final String[] noHostArgs = new String[] { CLUSTER_ID, TEMPLATE_ID, Integer.toString(MEMSIZE), Integer.toString(BOOTSEQUENCE), Integer.toString(N_SOCKETS), Integer.toString(N_CPUS), ISO_NAME, "-1", POOL_ID };

    private static final String ADD_COMMAND_PROLOG =
        "$templ = get-template -templateid \"" + TEMPLATE_ID + "\";$v = ";
    private static final String ADD_COMMAND_EPILOG =
        " -templateobject $templ -hostclusterid \"" + CLUSTER_ID + "\"";

    private static final String TEMPLATE_BY_NAME_ADD_COMMAND_PROLOG =
        "$t = select-template -searchtext \"name=" + TEMPLATE_NAME + "\";" +
        "$templ = get-template -templateid $t.TemplateId;$v = ";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_PROLOG =
        "$t = select-template -searchtext \"name=" + TEMPLATE_NAME + "\";" +
        "$c = select-cluster -searchtext \"name=" + CLUSTER_NAME + "\";" +
        "$templ = get-template -templateid $t.TemplateId;$v = ";

    private static final String CLUSTER_BY_NAME_ADD_COMMAND_EPILOG =
        " -templateobject $templ -hostclusterid $c.ClusterId";

    private static final String DISPLAY_ADD_COMMAND_EPILOG =
        " -numofmonitors 4 -displaytype VNC" + ADD_COMMAND_EPILOG;

    private static final String ASYNC_EPILOG =  ASYNC_ENDING + "$v;" + ASYNC_TASKS + "$v" + getProcess(Method.ADD);

    public PowerShellVmsResourceTest() {
        super(new PowerShellVmResource("0", null, null, null, null, null), "vms", "vm", extraArgs);
    }

    @Test
    public void testList() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        resource.setUriInfo(setUpResourceExpectations(getSelectCommand() + getProcess(Method.GET),
                                                      getSelectReturn(),
                                                      null,
                                                      NAMES));
        verifyCollection(resource.list().getVMs(), NAMES, DESCRIPTIONS);
    }

    @Test
    public void testListIncludeStats() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=statistics");
        resource.setUriInfo(setUpResourceExpectations(getSelectCommand() + getProcess(Method.GET, Detail.STATISTICS),
                                                      getSelectReturn(),
                                                      null,
                                                      NAMES));
        List<VM> vms = resource.list().getVMs();
        assertTrue(vms.get(0).isSetStatistics());
        verifyCollection(vms, NAMES, DESCRIPTIONS);
    }

    @Test
    public void testQuery() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        resource.setUriInfo(setUpResourceExpectations(getQueryCommand(VM.class) + getProcess(Method.GET),
                                                      getQueryReturn(),
                                                      getQueryParam(),
                                                      NAMES_SUBSET));
        verifyCollection(resource.list().getVMs(), NAMES_SUBSET, DESCRIPTIONS_SUBSET);
    }

    @Test
    public void testAddBlocking() throws Exception {
        setUpHttpHeaderExpectations("Expect", "201-created");
        setUpHttpHeaderNullExpectations("Accept");

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG + ";$v;$v" + getProcess(Method.ADD),
                getAddReturn(),
                NEW_NAME));
        Response response = resource.add(getModel(NEW_NAME, NEW_DESCRIPTION));
        verifyResponse(response, NEW_NAME, NEW_DESCRIPTION);
        assertEquals(201, response.getStatus());
    }

    @Test
    public void testAddWithTemplateId() throws Exception {
        setUpHttpHeaderNullExpectations("Expect", "Accept");

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        verifyCreated(resource.add(getModel(NEW_NAME, NEW_DESCRIPTION)), VM.class, NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithTemplateName() throws Exception {
        setUpHttpHeaderNullExpectations("Expect", "Accept");

        VM model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.getTemplate().setId(null);
        model.getTemplate().setName(TEMPLATE_NAME);

        resource.setUriInfo(setUpAddResourceExpectations(TEMPLATE_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand() + ADD_COMMAND_EPILOG + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        verifyCreated(resource.add(model), VM.class, NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithClusterName() throws Exception {
        setUpHttpHeaderNullExpectations("Expect", "Accept");

        VM model = getModel(NEW_NAME, NEW_DESCRIPTION);
        model.getTemplate().setId(null);
        model.getTemplate().setName(TEMPLATE_NAME);
        model.getCluster().setId(null);
        model.getCluster().setName(CLUSTER_NAME);

        resource.setUriInfo(setUpAddResourceExpectations(CLUSTER_BY_NAME_ADD_COMMAND_PROLOG + getAddCommand() + CLUSTER_BY_NAME_ADD_COMMAND_EPILOG + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        verifyCreated(resource.add(model), VM.class, NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddWithDisplay() throws Exception {
        setUpHttpHeaderNullExpectations("Expect", "Accept");

        resource.setUriInfo(setUpAddResourceExpectations(ADD_COMMAND_PROLOG + getAddCommand() + DISPLAY_ADD_COMMAND_EPILOG + ASYNC_EPILOG,
                                                         getAddReturn(),
                                                         false,
                                                         NEW_NAME));
        verifyCreated(resource.add(updateDisplay(getModel(NEW_NAME, NEW_DESCRIPTION))), VM.class, NEW_NAME, NEW_DESCRIPTION);
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        VM model = new VM();
        model.setName(NEW_NAME);
        resource.setUriInfo(setUpResourceExpectations(new String[]{}, new String[]{}, false, null));
        try {
            resource.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "VM", "add", "template.id|name", "cluster.id|name");
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpResourceExpectations(getRemoveCommand(), null);
        resource.remove(Integer.toString(NAMES[1].hashCode()));
    }

    @Test
    public void testGetSubResource() throws Exception {
        resource.setUriInfo(setUpResourceExpectations(null, null));
        verifyResource(
            (PowerShellVmResource)resource.getVmSubResource(Integer.toString(NEW_NAME.hashCode())),
            NEW_NAME);
    }

    protected PowerShellVmsResource getResource() {
        return new PowerShellVmsResource();
    }

    protected void populateModel(VM vm) {
        Template template = new Template();
        template.setId(TEMPLATE_ID);
        vm.setTemplate(template);

        Cluster cluster = new Cluster();
        cluster.setId(CLUSTER_ID);
        vm.setCluster(cluster);
    }

    private VM updateDisplay(VM vm) {
        vm.setDisplay(new Display());
        vm.getDisplay().setType(DisplayType.VNC.value());
        vm.getDisplay().setMonitors(4);
        return vm;
    }

    static String getProcess(Method method, Detail... details) {
        Set<Detail> detailSet = EnumSet.noneOf(Detail.class);
        for (Detail detail : details) {
            detailSet.add(detail);
        }
        return PowerShellVmsResource.getProcess(method, detailSet);
    }
}
