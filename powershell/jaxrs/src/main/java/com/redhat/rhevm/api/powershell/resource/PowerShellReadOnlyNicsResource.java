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

import java.util.List;

import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Nics;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.powershell.model.PowerShellNIC;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.resource.ReadOnlyDevicesResource;


public class PowerShellReadOnlyNicsResource extends AbstractPowerShellDevicesResource<NIC, Nics> implements ReadOnlyDevicesResource<NIC, Nics> {

    private String getCommand;

    public PowerShellReadOnlyNicsResource(String parentId,
                                          PowerShellPoolMap shellPools,
                                          PowerShellParser parser,
                                          String getCommand,
                                          UriInfoProvider uriProvider) {
        super(parentId, shellPools, parser, uriProvider);
        this.getCommand = getCommand;
    }

    public List<NIC> runAndParse(String command) {
        return PowerShellNIC.parse(getParser(), parentId, PowerShellCmd.runCommand(getPool(), command));
    }

    public NIC runAndParseSingle(String command) {
        List<NIC> nics = runAndParse(command);

        return !nics.isEmpty() ? nics.get(0) : null;
    }

    @Override
    public List<NIC> getDevices() {
        StringBuilder buf = new StringBuilder();

        buf.append("$v = " + getCommand + " " + PowerShellUtils.escape(parentId) + ";");
        buf.append("$v.GetNetworkAdapters()");

        return runAndParse(buf.toString());
    }

    /* Map the network names to network ID on the supplied network
     * interfaces. The powershell output only includes the network name.
     *
     * @param nic  the NIC to modify
     * @return     the modified NIC
     */
    private NIC lookupNetworkId(NIC nic) {
        StringBuilder buf = new StringBuilder();

        buf.append("$n = get-networks;");
        buf.append("foreach ($i in $n) {");
        buf.append("  if ($i.name -eq " + PowerShellUtils.escape(nic.getNetwork().getName()) + ") {");
        buf.append("    $i");
        buf.append("  }");
        buf.append("}");

        Network network = new Network();
        network.setId(PowerShellNetworkResource.runAndParseSingle(getPool(), getParser(), buf.toString()).getId());
        nic.setNetwork(network);

        return nic;
    }

    @Override
    public NIC addLinks(NIC nic) {
        return LinkHelper.addLinks(getUriInfo(), lookupNetworkId(nic));
    }

    @Override
    public Nics list() {
        Nics nics = new Nics();
        for (NIC nic : getDevices()) {
            nics.getNics().add(addLinks(nic));
        }
        return nics;
    }

    @Override
    public PowerShellReadOnlyDeviceResource<NIC, Nics> getDeviceSubResource(String id) {
        return new PowerShellReadOnlyDeviceResource<NIC, Nics>(this, id);
    }
}
