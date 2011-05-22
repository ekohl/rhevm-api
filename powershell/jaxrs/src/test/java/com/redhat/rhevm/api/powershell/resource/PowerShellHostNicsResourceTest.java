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

import java.net.URI;
import java.text.MessageFormat;
import java.util.concurrent.Executor;

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.Action;
import com.redhat.rhevm.api.model.Bonding;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostNIC;
import com.redhat.rhevm.api.model.HostNics;
import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.Slaves;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellException;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPool;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;

import org.junit.Test;

import org.junit.runner.RunWith;

import static org.easymock.EasyMock.expect;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { PowerShellCmd.class })
public class PowerShellHostNicsResourceTest
    extends AbstractPowerShellResourceTest<Host, PowerShellHostResource> {

    private static final String HOST_NAME = "1234";

    private static final String[] NICS = { "eth0", "eth1" };
    private static final String BOND_NAME = "bond0";
    private static final String[] NETS = { "net0", "net1" };
    private static final String[] MACS = { "00:31:20:5F:F9:DD", "00:31:20:5F:F9:EE" };
    private static final String[] IPS = { "192.168.1.7", "192.168.2.10" };
    private static final String[] SUBNETS = { "255.255.255.0", "255.255.255.0" };

    private static final String GET_NIC_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $n; break } }";
    private static final String GET_NICS_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; $h.getnetworkadapters()";
    private static final String UPDATE_NIC_CMD = "$h = get-host -hostid \"" + asId(HOST_NAME) + "\"; if($h -eq $null){throw 'Cannot find host \"" + asId(HOST_NAME) + "\"'};$na = $h.getnetworkadapters() | ? {$_.id -eq \"" + asId(NICS[0]) + "\"};if($na -eq $null){throw 'Cannot find nic with id \"" + asId(NICS[0]) + "\"'};$c = select-cluster | ? {$_.clusterid -eq $h.hostclusterid};if($c -eq $null){throw 'Cannot find cluster with id $h.hostclusterid.'};$network = get-networks -clusterid $c.clusterid | ? {$_.name -eq \"" + NETS[0] + "\"};$updated_host = update-logicalnetworktonetworkadapter -hostobject $h -network $network -networkadapterobjects $na -address \"" +IPS[0]+ "\" -subnet \"" +SUBNETS[0]+ "\" -gateway \"\" -bootprotocol \"StaticIp\" -oldnetworkname $na.network;$h.getnetworkadapters() | ? {$_.name -eq $na.name};";
    private static final String GET_NETWORK_CMD = "$n = get-networks; foreach ($i in $n) { if ($i.name -eq \"" + NETS[1] + "\") { $i; break } }";

    public static final String LOOKUP_NETWORK_ID_COMMAND = "$n = get-networks; foreach ($i in $n) '{' if ($i.name -eq \"{0}\") '{' $i; break '} }'";
    public static final String LOOKUP_SLAVE_ID_COMMAND = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in $h.getnetworkadapters()) { if ($n.name -eq \"" + NICS[0] + "\") { $n } if ($n.name -eq \"" + NICS[1] + "\") { $n } }";

    private static final String EMPTY_BOND_INTERFACES_XML = "<Property Name=\"BondInterfaces\" Type=\"System.String[]\" />";
    private static final String BOND_INTERFACES_XML = "<Property Name=\"BondInterfaces\" Type=\"System.String[]\">\n      <Property Type=\"System.String\">" + NICS[0] + "</Property>\n      <Property Type=\"System.String\">" + NICS[1] + "</Property>\n    </Property>";

    public static final String[][] extraArgs = new String[][] {
        { NETS[0], MACS[0], IPS[0], SUBNETS[0], "False", EMPTY_BOND_INTERFACES_XML },
        { NETS[1], MACS[1], IPS[1], SUBNETS[1], "False", EMPTY_BOND_INTERFACES_XML  },
    };

    private static final String[] bondArgs = new String[] {
        NETS[0], MACS[0], IPS[0], SUBNETS[0], "True", BOND_INTERFACES_XML
    };

    private static final String DATA_CENTER_ID = "999";

    private static final String[] networkArgs = new String[] { DATA_CENTER_ID };

    private static final String ATTACH_NETWORK_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.networkid -eq \"" + asId(NETS[0]) + "\") { $net = $n; break } } foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $nic = $n; break } } attach-logicalnetworktonetworkadapter -hostobject $h -network $net -networkadapter $nic";
    private static final String ATTACH_NETWORK_BY_NAME_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.name -eq \"" + NETS[0] + "\") { $net = $n; break } } foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $nic = $n; break } } attach-logicalnetworktonetworkadapter -hostobject $h -network $net -networkadapter $nic";
    private static final String DETACH_NETWORK_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.networkid -eq \"" + asId(NETS[0]) + "\") { $net = $n; break } } foreach ($n in $h.getnetworkadapters()) { if ($n.id -eq \"" + asId(NICS[0]) + "\") { $nic = $n; break } } detach-logicalnetworkfromnetworkadapter -hostobject $h -network $net -networkadapter $nic";
    private static final String ADD_BOND_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($n in get-networks) { if ($n.networkid -eq \"" + asId(NETS[0]) + "\") { $net = $n; break } } $nics = @(); foreach ($nic in $h.getnetworkadapters()) { if ($nic.id -eq \"" + asId(NICS[0]) + "\") { $nics += $nic } if ($nic.id -eq \"" + asId(NICS[1]) + "\") { $nics += $nic } } $h = add-bond -bondname \"" + BOND_NAME + "\" -hostobject $h -network $net -networkadapters $nics; foreach ($nic in $h.getnetworkadapters()) { if ($nic.name -eq \"" + BOND_NAME + "\") { $nic; break } }";
    private static final String REMOVE_BOND_CMD = "$h = get-host \"" + asId(HOST_NAME) + "\"; foreach ($nic in $h.getnetworkadapters()) { if ($nic.id -eq \"" + asId(BOND_NAME) + "\") { $bond = $nic; break } } remove-bond -hostobject $h -bondobject $bond";

    private static final String NIC_URI = "hosts/" + asId(HOST_NAME) + "/nics/" + asId(NICS[0]);
    private static final String ACTION_RETURN = "replace with realistic powershell return";
    private static final String FAILURE = "replace with realistic powershell failure";
    private static final String REASON = "Powershell command \"" + ATTACH_NETWORK_CMD + "\" failed with " + FAILURE;
    private static final String DETAIL = "at com.redhat.rhevm.api.powershell.util.PowerShellCmd.runCommand(";

    protected PowerShellHostResource getResource(Executor executor, PowerShellPoolMap poolMap, PowerShellParser parser, UriInfoProvider uriProvider) {
        return new PowerShellHostResource(asId(HOST_NAME), executor, uriProvider, poolMap, parser, httpHeaders);
    }

    protected String formatNics(String[] names, String[][] args) {
        String[] descriptions = new String[names.length];
        return formatXmlReturn("hostnic", names, descriptions, args);
    }

    protected String formatNic(String name, String[] args) {
        return formatNics(asArray(name), asArray(args));
    }

    protected String formatNetwork(String name) {
        return formatXmlReturn("network",
                               new String[] { name },
                               new String[] { "" },
                               networkArgs);
    }

    protected String formatLookupNetworkCmd(int i) {
        return MessageFormat.format(LOOKUP_NETWORK_ID_COMMAND, NETS[i]);
    }

    @Test
    public void testGet() {
        PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellHostNicResource nicResource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent, uriProvider);

        String[] commands = { GET_NIC_CMD,
                              formatLookupNetworkCmd(0) };
        String[] returns = { formatNic(NICS[0], extraArgs[0]),
                             formatNetwork(NETS[0]) };

        setUpCmdExpectations(commands, returns);
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyHostNic(nicResource.get(), 0);
    }

    @Test
    public void testUpdate() {
        setUpHttpHeaderNullExpectations("Accept");
        setUriInfo(setUpBasicUriExpectations());
        PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellHostNicResource nicResource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent, uriProvider);

        setUpCmdExpectations(new String[]{ UPDATE_NIC_CMD, GET_NETWORK_CMD },
                                            new String[]{ formatNic(NICS[0], extraArgs[1]), formatNetwork(NETS[1]) });

        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyUpdatedHostNic((nicResource.update(createNic(IPS[0], SUBNETS[0], "", NETS[0]))), 1);
    }

    private HostNIC createNic(String addr, String mask, String gw, String network) {
      HostNIC nic = new HostNIC();
      nic.setId(asId(NICS[0]));
      nic.setIp(new IP());
      nic.getIp().setAddress(addr);
      nic.getIp().setNetmask(mask);
      nic.getIp().setGateway(gw);
      nic.setNetwork(new Network());
      nic.getNetwork().setName(network);

      return nic;
    }

    @Test
    public void testHostNicsList() {
        PowerShellHostNicsResource nicResource = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);

        String[] commands = { GET_NICS_CMD,
                              formatLookupNetworkCmd(0),
                              formatLookupNetworkCmd(1) };
        String[] returns = { formatNics(NICS, extraArgs),
                             formatNetwork(NETS[0]),
                             formatNetwork(NETS[1]) };

        setUpCmdExpectations(commands, returns);
        setUriInfo(setUpBasicUriExpectations());
        replayAll();

        verifyHostNics(nicResource.list());
    }

    @Test
    public void testHostNicAdd() throws Exception {
        PowerShellHostNicsResource nicResource = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);

        String[] commands = { ADD_BOND_CMD,
                              LOOKUP_SLAVE_ID_COMMAND,
                              formatLookupNetworkCmd(0) };
        String[] returns = { formatNic(BOND_NAME, bondArgs),
                             formatNics(NICS, extraArgs),
                             formatNetwork(NETS[0]) };

        setUpCmdExpectations(commands, returns);
        setUriInfo(setUpUriInfoExpections(asId(BOND_NAME)));
        replayAll();

        HostNIC bond = new HostNIC();
        bond.setName(BOND_NAME);
        bond.setNetwork(new Network());
        bond.getNetwork().setId(asId(NETS[0]));
        bond.setBonding(new Bonding());
        bond.getBonding().setSlaves(new Slaves());

        HostNIC slave = new HostNIC();
        slave.setId(asId(NICS[0]));
        bond.getBonding().getSlaves().getSlaves().add(slave);

        slave = new HostNIC();
        slave.setId(asId(NICS[1]));
        bond.getBonding().getSlaves().getSlaves().add(slave);

        verifyBond((HostNIC)nicResource.add(bond).getEntity());
    }

    @Test
    public void testHostNicRemove() {
        PowerShellHostNicsResource nicResource = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);

        setUpCmdExpectations(REMOVE_BOND_CMD, "");
        replayAll();

        nicResource.remove(asId(BOND_NAME));
    }

    @Test
    public void testAttach() throws Exception {
        PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellHostNicResource nicResource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent, uriProvider);

        Action action = getAction();
        action.setNetwork(new Network());
        action.getNetwork().setId(asId(NETS[0]));

        setUriInfo(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_CMD, ACTION_RETURN));
        verifyActionResponse(nicResource.attach(action), NIC_URI, false);
    }

    @Test
    public void testAttachByName() throws Exception {
        PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellHostNicResource nicResource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent, uriProvider);

        Action action = getAction();
        action.setNetwork(new Network());
        action.getNetwork().setName(NETS[0]);

        setUriInfo(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_BY_NAME_CMD, ACTION_RETURN));
        verifyActionResponse(nicResource.attach(action), NIC_URI, false);
    }

    @Test
    public void testDetach() throws Exception {
        PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellHostNicResource nicResource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent, uriProvider);

        Action action = getAction();
        action.setNetwork(new Network());
        action.getNetwork().setId(asId(NETS[0]));

        setUriInfo(setUpActionExpectation(NIC_URI, "detach", DETACH_NETWORK_CMD, ACTION_RETURN));
        verifyActionResponse(nicResource.detach(action), NIC_URI, false);
    }

    @Test
    public void testAttachAsync() throws Exception {
        PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellHostNicResource nicsResource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent, uriProvider);

        Action action = getAction(true);
        action.setNetwork(new Network());
        action.getNetwork().setId(asId(NETS[0]));

        setUriInfo(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_CMD, ACTION_RETURN));
        verifyActionResponse(nicsResource.attach(action), NIC_URI, true);
    }

    @Test
    public void testAttachAsyncFailed() throws Exception {
        PowerShellHostNicsResource parent = new PowerShellHostNicsResource(asId(HOST_NAME), executor, poolMap, parser, uriProvider);
        PowerShellHostNicResource nicResource = new PowerShellHostNicResource(asId(NICS[0]), executor, poolMap, parser, parent, uriProvider);

        Action action = getAction(true);
        action.setNetwork(new Network());
        action.getNetwork().setId(asId(NETS[0]));

        setUriInfo(setUpActionExpectation(NIC_URI, "attach", ATTACH_NETWORK_CMD, new PowerShellException(FAILURE)));
        verifyActionResponse(nicResource.attach(action), NIC_URI, true, REASON, DETAIL);
    }

    private UriInfo setUpUriInfoExpections(String id) throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriInfo.getAbsolutePathBuilder()).andReturn(uriBuilder);
        expect(uriBuilder.path(id)).andReturn(uriBuilder);
        expect(uriBuilder.build()).andReturn(new URI("hosts/" + asId(HOST_NAME) + "/nics/" + id)).anyTimes();
        return uriInfo;
    }

    private void setUpCmdExpectations(String command, String ret) {
        setUpCmdExpectations(asArray(command), asArray(ret));
    }

    private void setUpCmdExpectations(String commands[], String returns[]) {
        mockStatic(PowerShellCmd.class);
        for (int i = 0 ; i < Math.min(commands.length, returns.length) ; i++) {
            if (commands[i] != null) {
                expect(PowerShellCmd.runCommand(setUpPoolExpectations(), commands[i])).andReturn(returns[i]);
            }
        }
    }

    protected PowerShellPool setUpPoolExpectations() {
        PowerShellPool pool = createMock(PowerShellPool.class);
        expect(poolMap.get()).andReturn(pool);
        return pool;
    }

    private void verifyHostNicDetails(HostNIC nic, int i) {
        assertNotNull(nic.getHost());
        assertEquals(asId(HOST_NAME), nic.getHost().getId());
        assertNotNull(nic.getNetwork());
        assertEquals(asId(NETS[i]), nic.getNetwork().getId());
        assertNotNull(nic.getMac());
        assertEquals(MACS[i], nic.getMac().getAddress());
        assertNotNull(nic.getIp());
        assertEquals(IPS[i], nic.getIp().getAddress());
        assertEquals(SUBNETS[i], nic.getIp().getNetmask());
    }

    private void verifyBond(HostNIC nic) {
        assertNotNull(nic);
        assertEquals(asId(BOND_NAME), nic.getId());
        assertNotNull(nic.getHref());
        verifyHostNicDetails(nic, 0);
        assertNotNull(nic.getBonding());
        assertNotNull(nic.getBonding().getSlaves());
        assertEquals(2, nic.getBonding().getSlaves().getSlaves().size());
        assertEquals(asId(NICS[0]), nic.getBonding().getSlaves().getSlaves().get(0).getId());
        assertNotNull(nic.getBonding().getSlaves().getSlaves().get(0).getHref());
        assertEquals(asId(NICS[1]), nic.getBonding().getSlaves().getSlaves().get(1).getId());
        assertNotNull(nic.getBonding().getSlaves().getSlaves().get(0).getHref());
    }

    private void verifyHostNic(HostNIC nic, int i) {
        assertNotNull(nic);
        assertEquals(asId(NICS[i]), nic.getId());
        verifyHostNicDetails(nic, i);
        verifyLinks(nic);
    }

    private void verifyUpdatedHostNic(HostNIC nic, int i) {
        assertNotNull(nic);
        assertNotNull(nic.getNetwork());
        assertNotNull(nic.getIp());
        assertEquals(nic.getNetwork().getId(), "3377716");
        assertEquals(nic.getIp().getAddress(), IPS[i]);
        assertEquals(nic.getIp().getNetmask(), SUBNETS[i]);
    }

    private void verifyHostNics(HostNics nics) {
        assertNotNull(nics);
        assertEquals(NICS.length, nics.getHostNics().size());
        for (int i = 0; i < NICS.length; i++) {
            verifyHostNic(nics.getHostNics().get(i), i);
        }
    }
}
