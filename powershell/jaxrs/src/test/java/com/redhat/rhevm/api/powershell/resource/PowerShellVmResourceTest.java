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

import java.text.MessageFormat;
import java.util.concurrent.Executor;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.CdRoms;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.Fault;
import com.redhat.rhevm.api.model.File;
import com.redhat.rhevm.api.model.Floppies;
import com.redhat.rhevm.api.model.Floppy;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Boot;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Ticket;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.model.VmType;
import com.redhat.rhevm.api.powershell.resource.PowerShellVmsResource.Detail;
import com.redhat.rhevm.api.powershell.resource.PowerShellVmsResource.Method;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import static com.redhat.rhevm.api.powershell.resource.PowerShellVmsResourceTest.getProcess;
import static org.easymock.classextension.EasyMock.expect;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

public class PowerShellVmResourceTest extends AbstractPowerShellResourceTest<VM, PowerShellVmResource> {

    private static final String VM_NAME = "sedna";
    private static final String VM_ID = Integer.toString(VM_NAME.hashCode());
    private static final String NEW_NAME = "eris";
    private static final String CLUSTER_ID = PowerShellVmsResourceTest.CLUSTER_ID;
    private static final String TEMPLATE_ID = PowerShellVmsResourceTest.TEMPLATE_ID;
    private static final String STORAGE_DOMAIN_NAME = "xtratime";
    private static final String STORAGE_DOMAIN_ID = Integer.toString(STORAGE_DOMAIN_NAME.hashCode());
    private static final String BAD_ID = "98765";
    private static final String TICKET_VALUE = "flibbertigibbet";

    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String FAILURE = "replace with realistic powershell failure";
    private static final String REASON = "Powershell command \"start-vm -vmid \"" + VM_ID + "\"\" failed with " + FAILURE;
    private static final String DETAIL = "at com.redhat.rhevm.api.powershell.util.PowerShellCmd.runCommand(";
    private static final String UPDATE_COMMAND_TEMPLATE = "$v = get-vm \"" + VM_ID + "\";$v.name = \"" + NEW_NAME + "\";{0}update-vm -vmobject $v";
    private static final String UPDATE_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, "");
    private static final String UPDATE_DISPLAY_COMMAND = MessageFormat.format(UPDATE_COMMAND_TEMPLATE, " $v.numofmonitors = 4; $v.displaytype = 'VNC';");
    private static final String TICKET_COMMAND = "set-vmticket -vmid \"" + VM_ID + "\" -ticket \"" + TICKET_VALUE + "\"";
    private static final String TICKET_EXPIRY_COMMAND = "set-vmticket -vmid \"" + VM_ID + "\" -validtime \"360\"";
    private static final String TICKET_RETURN = "<Objects><Object Type=\"RhevmCmd.CLIVmTicket\">"
        + "<Property Name=\"VmId\" Type=\"System.Guid\">" + VM_ID + "</Property>"
        + "<Property Name=\"Ticket\" Type=\"System.String\">" + TICKET_VALUE + "</Property>"
        + "<Property Name=\"ValidTime\" Type=\"System.Int32\">360</Property></Object></Objects>";

    private static final String DEST_HOST_ID = "1337";
    private static final String DEST_HOST_NAME = "farawaysoclose";
    private static final String MIGRATE_COMMAND = "migrate-vm -vmid \"" + VM_ID + "\" -desthostid \"" + DEST_HOST_ID + "\"";
    private static final String EXPORT_WITH_STORAGE_DOMAIN_COMMAND = "export-vm -vmid \"" + VM_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\"";
    private static final String MOVE_WITH_STORAGE_DOMAIN_COMMAND = "move-vmimages -vmid \"" + VM_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\"";
    private static final String EXPORT_WITH_NAMED_STORAGE_DOMAIN_COMMAND = "$dest = select-storagedomain | ? { $_.name -eq \"" + STORAGE_DOMAIN_NAME + "\" }; export-vm -vmid \"" + VM_ID + "\" -storagedomainid $dest.storagedomainid";
    private static final String MOVE_WITH_NAMED_STORAGE_DOMAIN_COMMAND = "$dest = select-storagedomain | ? { $_.name -eq \"" + STORAGE_DOMAIN_NAME + "\" }; if($dest -eq $null) {throw 'Cannot find storagedomain.'}; move-vmimages -vmid \"" + VM_ID + "\" -storagedomainid $dest.storagedomainid";
    private static final String EXPORT_WITH_PARAMS_COMMAND = "export-vm -vmid \"" + VM_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\"";
    private static final String MOVE_WITH_PARAMS_COMMAND = "move-vmimages -vmid \"" + VM_ID + "\" -storagedomainid \"" + STORAGE_DOMAIN_ID + "\"";
    private static final String MIGRATE_COMMAND_WITH_HOST_NAME =
        "$h = select-host -searchtext \"name=" + DEST_HOST_NAME + "\";" +
        "migrate-vm -vmid \"" + VM_ID + "\" -desthostid $h.hostid";

    private static final String CDROM_FILE_ID = "foo.iso";
    private static final String FLOPPY_FILE_ID = "bar.vfd";

    private static final String START_WITH_PARAMS = "start-vm -vmid \"" + VM_ID + "\" -runandpause -defaulthostid \"" + DEST_HOST_ID + "\" -runasstateless -displaytype 'VNC' -bootdevice 'D' -isofilename \"" + CDROM_FILE_ID + "\" -floppypath \"" + FLOPPY_FILE_ID + "\"";

    protected PowerShellVmResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellVmResource(VM_ID, executor, uriProvider, poolMap, parser, httpHeaders);
    }

    protected String formatVm(String name) {
        return formatVm(name, PowerShellVmsResourceTest.extraArgs);
    }

    protected String formatVm(String name, String[] extraArgs) {
        return formatXmlReturn("vm",
                               new String[] { name },
                               new String[] { "" },
                               extraArgs);
    }

    @Test
    public void testGet() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + getProcess(Method.GET),
                                       formatVm(VM_NAME),
                                       VM_NAME));
        verifyVM(resource.get(), VM_NAME);
    }

    @Test
    public void testGetIncludeStatistics() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=statistics");
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + getProcess(Method.GET, Detail.STATISTICS),
                                       formatVm(VM_NAME),
                                       VM_NAME));
        VM vm = resource.get();
        assertFalse(vm.isSetDisks());
        assertFalse(vm.isSetNics());
        assertTrue(vm.isSetStatistics());
        verifyVM(vm, VM_NAME);
    }

    @Test
    public void testGetIncludeDisks() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=disks");
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + getProcess(Method.GET, Detail.DISKS),
                                       formatVm(VM_NAME),
                                       VM_NAME));
        VM vm = resource.get();
        assertTrue(vm.isSetDisks());
        assertFalse(vm.isSetNics());
        assertFalse(vm.isSetStatistics());
        verifyVM(vm, VM_NAME);
    }

    @Test
    public void testGetIncludeNics() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=nics");
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + getProcess(Method.GET, Detail.NICS),
                                       formatVm(VM_NAME),
                                       VM_NAME));
        VM vm = resource.get();
        assertFalse(vm.isSetDisks());
        assertTrue(vm.isSetNics());
        assertFalse(vm.isSetStatistics());
        verifyVM(vm, VM_NAME);
    }

    @Test
    public void testGetIncludeDisksAndStatistics() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=disks; detail=statistics");
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + getProcess(Method.GET, Detail.STATISTICS, Detail.DISKS),
                                       formatVm(VM_NAME),
                                       VM_NAME));
        VM vm = resource.get();
        assertTrue(vm.isSetDisks());
        assertFalse(vm.isSetNics());
        assertTrue(vm.isSetStatistics());
        verifyVM(vm, VM_NAME);
    }

    @Test
    public void testGetIncludeDisksNicsAndStatistics() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=disks; detail=nics; detail=statistics");
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + getProcess(Method.GET, Detail.STATISTICS, Detail.NICS, Detail.DISKS),
                                       formatVm(VM_NAME),
                                       VM_NAME));
        VM vm = resource.get();
        assertTrue(vm.isSetDisks());
        assertTrue(vm.isSetNics());
        assertTrue(vm.isSetStatistics());
        verifyVM(vm, VM_NAME);
    }

    public void testGetNoHost() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        setUriInfo(setUpVmExpectations("get-vm \"" + VM_ID + "\"" + getProcess(Method.GET),
                                       formatVm(VM_NAME, PowerShellVmsResourceTest.noHostArgs),
                                       VM_NAME));
        verifyVM(resource.get(), VM_NAME, false);
    }

    @Test
    public void testGoodUpdate() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        setUriInfo(setUpVmExpectations(UPDATE_COMMAND + getProcess(Method.GET),
                                       formatVm(NEW_NAME),
                                       NEW_NAME));
        verifyVM(resource.update(getVM(NEW_NAME)), NEW_NAME);
    }

    @Test
    public void testUpdateIncludeStatistics() throws Exception {
        setUpHttpHeaderExpectations("Accept", "application/xml; detail=statistics");
        setUriInfo(setUpVmExpectations(UPDATE_COMMAND + getProcess(Method.GET, Detail.STATISTICS),
                                       formatVm(NEW_NAME),
                                       NEW_NAME));
        VM vm = resource.update(getVM(NEW_NAME));
        assertTrue(vm.isSetStatistics());
        verifyVM(vm, NEW_NAME);
    }

    @Test
    public void testUpdateDisplay() throws Exception {
        setUpHttpHeaderNullExpectations("Accept");
        setUriInfo(setUpVmExpectations(UPDATE_DISPLAY_COMMAND + getProcess(Method.GET),
                                       formatVm(NEW_NAME),
                                       NEW_NAME));
        verifyVM(resource.update(updateDisplay(getVM(NEW_NAME))), NEW_NAME);
    }

    @Test
    public void testBadIdUpdate() throws Exception {
        VM update = getVM(BAD_ID, NEW_NAME);
        doTestBadUpdate(update, "id");
    }

    @Test
    public void testBadTypeUpdate() throws Exception {
        VM update = getVM(NEW_NAME);
        update.setType(VmType.DESKTOP.value());
        doTestBadUpdate(update, "type");
    }

    public void doTestBadUpdate(VM update, String field) throws Exception {
        try {
            setUriInfo(createMock(UriInfo.class));
            replayAll();
            resource.update(update);
            fail("expected WebApplicationException on bad update");
        } catch (WebApplicationException wae) {
            verifyUpdateException(wae, field);
        }
    }

    @Test
    public void testStart() throws Exception {
        setUriInfo(setUpActionExpectation("start", "start-vm"));
        verifyActionResponse(resource.start(getAction()), false);
    }

    @Test
    public void testStartWithParams() throws Exception {
        VM vm = new VM();
        vm.setHost(new Host());
        vm.getHost().setId(DEST_HOST_ID);
        vm.setStateless(true);
        vm.setDisplay(new Display());
        vm.getDisplay().setType(DisplayType.VNC.value());
        vm.setOs(new OperatingSystem());
        vm.getOs().getBoot().add(new Boot());
        vm.getOs().getBoot().get(0).setDev(BootDevice.CDROM.value());
        vm.setCdroms(new CdRoms());
        vm.getCdroms().getCdRoms().add(new CdRom());
        vm.getCdroms().getCdRoms().get(0).setFile(new File());
        vm.getCdroms().getCdRoms().get(0).getFile().setId(CDROM_FILE_ID);
        vm.setFloppies(new Floppies());
        vm.getFloppies().getFloppies().add(new Floppy());
        vm.getFloppies().getFloppies().get(0).setFile(new File());
        vm.getFloppies().getFloppies().get(0).getFile().setId(FLOPPY_FILE_ID);

        Action action = getAction();
        action.setPause(true);
        action.setVm(vm);

        setUriInfo(setUpActionExpectation("start", START_WITH_PARAMS, false, null));
        verifyActionResponse(resource.start(action), false);
    }

    @Test
    public void testStop() throws Exception {
        setUriInfo(setUpActionExpectation("stop", "stop-vm"));
        verifyActionResponse(resource.stop(getAction()), false);
    }

    @Test
    public void testShutdown() throws Exception {
        setUriInfo(setUpActionExpectation("shutdown", "shutdown-vm"));
        verifyActionResponse(resource.shutdown(getAction()), false);
    }

    @Test
    public void testSuspend() throws Exception {
        setUriInfo(setUpActionExpectation("suspend", "suspend-vm"));
        verifyActionResponse(resource.suspend(getAction()), false);
    }

    @Test
    public void testDetach() throws Exception {
        setUriInfo(setUpActionExpectation("detach", "detach-vm"));
        verifyActionResponse(resource.detach(getAction()), false);
    }

    @Test
    public void testMigrate() throws Exception {
        Action action = getAction();
        action.setHost(new Host());
        action.getHost().setId(DEST_HOST_ID);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND, false, null));
        verifyActionResponse(resource.migrate(action), false);
    }

    @Test
    public void testMigrateWithHostName() throws Exception {
        Action action = getAction();
        action.setHost(new Host());
        action.getHost().setName(DEST_HOST_NAME);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND_WITH_HOST_NAME, false, null));
        verifyActionResponse(resource.migrate(action), false);
    }

    @Test
    public void testIncompleteMigrate() throws Exception {
        setUriInfo(setUpActionExpectation(null, null, null, null));
        try {
            resource.migrate(getAction());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "migrate", "host.id|name");
        }
    }

    @Test
    public void testExportWithStorageDomain() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(STORAGE_DOMAIN_ID);
        setUriInfo(setUpActionExpectation("export", EXPORT_WITH_STORAGE_DOMAIN_COMMAND, false, null));
        verifyActionResponse(resource.export(action), false);
    }

    @Test
    public void testExportWithNamedStorageDomain() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setName(STORAGE_DOMAIN_NAME);
        setUriInfo(setUpActionExpectation("export", EXPORT_WITH_NAMED_STORAGE_DOMAIN_COMMAND, false, null));
        verifyActionResponse(resource.export(action), false);
    }

    @Test
    public void testExportWithParams() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(STORAGE_DOMAIN_ID);
        setUriInfo(setUpActionExpectation("export", EXPORT_WITH_PARAMS_COMMAND, false, null));
        verifyActionResponse(resource.export(action), false);
    }

    @Test
    public void testIncompleteExport() throws Exception {
        setUriInfo(setUpActionExpectation(null, null, null, null));
        try {
            resource.export(getAction());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "export", "storageDomain.id|name");
        }
    }

    @Test
    public void testMoveAsync() throws Exception {
        Action action = getAction(true);
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(STORAGE_DOMAIN_ID);
        setUriInfo(setUpActionExpectation("move", MOVE_WITH_STORAGE_DOMAIN_COMMAND, false, null));
        verifyActionResponse(resource.move(action), true);
    }

        @Test
    public void testMoveWithNamedStorageDomain() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setName(STORAGE_DOMAIN_NAME);
        setUriInfo(setUpActionExpectation("move", MOVE_WITH_NAMED_STORAGE_DOMAIN_COMMAND, false, null));
        verifyActionResponse(resource.move(action), false);
    }

        @Test
    public void testMoveWithParams() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(STORAGE_DOMAIN_ID);
        action.setDiscardSnapshots(true);
        action.setExclusive(true);
        setUriInfo(setUpActionExpectation("move", MOVE_WITH_PARAMS_COMMAND, false, null));
        verifyActionResponse(resource.move(action), false);
    }

        @Test
    public void testMoveWithStorageDomain() throws Exception {
        Action action = getAction();
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(STORAGE_DOMAIN_ID);
        setUriInfo(setUpActionExpectation("move", MOVE_WITH_STORAGE_DOMAIN_COMMAND, false, null));
        verifyActionResponse(resource.move(action), false);
    }

        @Test
    public void testIncompleteMove() throws Exception {
        setUriInfo(setUpActionExpectation(null, null, null, null));
        try {
            resource.move(getAction());
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Action", "move", "storageDomain.id|name");
        }
    }

    @Test
    public void testTicket() throws Exception {
        Action action = getAction();
        action.setTicket(new Ticket());
        action.getTicket().setValue(TICKET_VALUE);
        setUriInfo(setUpActionExpectation("ticket", TICKET_COMMAND, TICKET_RETURN, false, null));
        Response response = resource.ticket(action);
        verifyActionResponse(response, false);
        verifyTicketResponse(response);
    }

    @Test
    public void testStartAsync() throws Exception {
        setUriInfo(setUpActionExpectation("start", "start-vm"));
        verifyActionResponse(resource.start(getAction(true)), true);
    }

    @Test
    public void testStartAsyncFailed() throws Exception {
        setUriInfo(setUpActionExpectation("start", "start-vm", new PowerShellException(FAILURE)));
        verifyActionResponse(resource.start(getAction(true)), true, REASON, DETAIL);
    }

    @Test
    public void testStopAsync() throws Exception {
        setUriInfo(setUpActionExpectation("stop", "stop-vm"));
        verifyActionResponse(resource.stop(getAction(true)), true);
    }

    @Test
    public void testShutdownAsync() throws Exception {
        setUriInfo(setUpActionExpectation("shutdown", "shutdown-vm"));
        verifyActionResponse(resource.shutdown(getAction(true)), true);
    }

    @Test
    public void testSuspendAsync() throws Exception {
        setUriInfo(setUpActionExpectation("suspend", "suspend-vm"));
        verifyActionResponse(resource.suspend(getAction(true)),true);
    }

    @Test
    public void testDetachAsync() throws Exception {
        setUriInfo(setUpActionExpectation("detach", "detach-vm"));
        verifyActionResponse(resource.detach(getAction(true)), true);
    }

    @Test
    public void testMigrateAsync() throws Exception {
        Action action = getAction(true);
        action.setHost(new Host());
        action.getHost().setId(DEST_HOST_ID);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND, false, null));
        verifyActionResponse(resource.migrate(action), true);
    }

    @Test
    public void testMigrateAsyncWithHostName() throws Exception {
        Action action = getAction(true);
        action.setHost(new Host());
        action.getHost().setName(DEST_HOST_NAME);
        setUriInfo(setUpActionExpectation("migrate", MIGRATE_COMMAND_WITH_HOST_NAME, false, null));
        verifyActionResponse(resource.migrate(action), true);
    }

    @Test
    public void testExportAsync() throws Exception {
        Action action = getAction(true);
        action.setStorageDomain(new StorageDomain());
        action.getStorageDomain().setId(STORAGE_DOMAIN_ID);
        setUriInfo(setUpActionExpectation("export", EXPORT_WITH_STORAGE_DOMAIN_COMMAND, false, null));
        verifyActionResponse(resource.export(action), true);
    }

    @Test
    public void testTicketAsync() throws Exception {
        Action action = getAction(true);
        action.setTicket(new Ticket());
        action.getTicket().setExpiry(360L);
        setUriInfo(setUpActionExpectation("ticket", TICKET_EXPIRY_COMMAND, TICKET_RETURN, false, null));
        verifyActionResponse(resource.ticket(action), true);
    }

    private UriInfo setUpVmExpectations(String command, String ret, String name) throws Exception {
        mockStatic(PowerShellCmd.class);
        expect(PowerShellCmd.runCommand(setUpPoolExpectations(), command)).andReturn(ret);
        UriInfo uriInfo = setUpBasicUriExpectations();
        replayAll();
        return uriInfo;
    }

    private UriInfo setUpActionExpectation(String verb, String command, boolean appendVmId, Throwable t) throws Exception {
        return setUpActionExpectation(verb, command, ACTION_RETURN, appendVmId, t);
    }

    private UriInfo setUpActionExpectation(String verb, String command, String ret, boolean appendVmId, Throwable t) throws Exception {
        if (appendVmId) {
            command += " -vmid \"" + VM_ID + "\"";
        }
        if (t == null) {
            return setUpActionExpectation("/vms/" + VM_ID + "/", verb, command, ret);
        } else {
            return setUpActionExpectation("/vms/" + VM_ID + "/", verb, command, t);
        }
    }

    private UriInfo setUpActionExpectation(String verb, String command) throws Exception {
        return setUpActionExpectation(verb, command, true, null);
    }

    private UriInfo setUpActionExpectation(String verb, String command, Throwable t) throws Exception {
        return setUpActionExpectation(verb, command, true, t);
    }

    private VM getVM(String name) {
        return getVM(VM_ID, name);
    }

    private VM getVM(String id, String name) {
        VM vm = new VM();
        vm.setId(id);
        vm.setName(name);
        return vm;
    }

    private VM updateDisplay(VM vm) {
        vm.setDisplay(new Display());
        vm.getDisplay().setType(DisplayType.VNC.value());
        vm.getDisplay().setMonitors(4);
        return vm;
    }

    private void verifyVM(VM vm, String name) {
        verifyVM(vm, name, true);
    }

    private void verifyVM(VM vm, String name, boolean hostExpected) {
        assertNotNull(vm);
        assertEquals(Integer.toString(name.hashCode()), vm.getId());
        assertEquals(name, vm.getName());
        assertTrue(vm.isSetDisplay());
        assertEquals(DisplayType.SPICE.value(), vm.getDisplay().getType());
        assertEquals(Integer.valueOf(1), vm.getDisplay().getMonitors());
        assertTrue(vm.getDisplay().getPort() == null || vm.getDisplay().getPort() != -1);
        if (hostExpected) {
            assertTrue(vm.isSetHost());
        } else {
            assertFalse(vm.isSetHost());
        }
        verifyLinks(vm);
    }

    private void verifyActionResponse(Response r, boolean async) throws Exception {
        verifyActionResponse(r, "vms/" + VM_ID, async);
    }

    private void verifyActionResponse(Response r, boolean async, String reason, String detailExerpt) throws Exception {
        verifyActionResponse(r, "vms/" + VM_ID, async, reason, detailExerpt);
    }

    private void verifyTicketResponse(Response r) {
        Action action = (Action)r.getEntity();
        assertTrue(action.isSetTicket());
        assertEquals(TICKET_VALUE, action.getTicket().getValue());
    }

    private void verifyUpdateException(WebApplicationException wae, String field) {
        assertEquals(409, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Broken immutability constraint", fault.getReason());
        assertEquals("Attempt to set immutable field: " + field, fault.getDetail());
    }

    protected static String[] asArray(String s) {
        return new String[] { s };
    }
}
