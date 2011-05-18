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

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhevm.api.model.BootProtocol;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.HostNIC;
import com.redhat.rhevm.api.model.IP;
import com.redhat.rhevm.api.model.MAC;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;

public class PowerShellHostNIC extends HostNIC {

    private String bondName;
    private List<String> bondInterfaces;

    public String getBondName() {
        return bondName;
    }
    public void setBondName(String bondName) {
        this.bondName = bondName;
    }

    public List<String> getBondInterfaces() {
        if (bondInterfaces == null) {
            bondInterfaces = new ArrayList<String>();
        }
        return bondInterfaces;
    }
    public void unsetBondInterfaces() {
        bondInterfaces = null;
    }

    public static List<PowerShellHostNIC> parse(PowerShellParser parser, String hostId, String output) {
        List<PowerShellHostNIC> ret = new ArrayList<PowerShellHostNIC>();

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            PowerShellHostNIC nic = new PowerShellHostNIC();

            nic.setId(entity.get("id"));
            nic.setName(entity.get("name"));

            nic.setHost(new Host());
            nic.getHost().setId(hostId);

            if (entity.get("network") != null) {
                Network network = new Network();
                network.setName(entity.get("network"));
                nic.setNetwork(network);
            }

            if (entity.get("macaddress") != null) {
                MAC mac = new MAC();
                mac.setAddress(entity.get("macaddress"));
                nic.setMac(mac);
            }

            if (entity.get("address") != null ||
                entity.get("subnet") != null ||
                entity.get("gateway") != null) {
                IP ip = new IP();
                ip.setAddress(entity.get("address"));
                ip.setNetmask(entity.get("subnet"));
                ip.setGateway(entity.get("gateway"));
                nic.setIp(ip);
            }

            nic.setBondName(entity.get("bondname"));
            Integer speed = entity.get("speed", Integer.class);
            if (speed != null && speed > 0) {
                nic.setSpeed(speed * 1000L * 1000);
            }

            List<String> ifaces = entity.get("bondinterfaces", List.class);
            for (String iface : ifaces) {
                nic.getBondInterfaces().add(iface);
            }

            ret.add(nic);
        }

        return ret;
    }

    public static String buildNetworkBootProtocol(BootProtocol protocol) {
        if (protocol != null) {
            switch (protocol) {
                case DHCP:
                    return "Dhcp";
                case STATIC:
                    return "StaticIp";
                default:
                    break;
            }
        }
        return null;
    }
}
