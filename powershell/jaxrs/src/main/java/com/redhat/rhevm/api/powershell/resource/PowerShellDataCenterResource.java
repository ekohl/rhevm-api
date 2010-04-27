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
import java.util.ArrayList;

import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.powershell.model.PowerShellDataCenter;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

public class PowerShellDataCenterResource implements DataCenterResource {
    /* FIXME: would like to do:
     * private @Context UriInfo uriInfo;
     */

    private String id;

    public PowerShellDataCenterResource(String id) {
        this.id = id;
    }

    public static ArrayList<DataCenter> runAndParse(String command) {
        return PowerShellDataCenter.parse(PowerShellUtils.runCommand(command));
    }

    public static DataCenter runAndParseSingle(String command) {
        ArrayList<DataCenter> datacenters = runAndParse(command);

        return !datacenters.isEmpty() ? datacenters.get(0) : null;
    }

    public static DataCenter addLink(DataCenter datacenter, URI uri) {
        datacenter.setLink(new Link("self", uri));
        return new DataCenter(datacenter);
    }

    @Override
    public DataCenter get(UriInfo uriInfo) {
        return addLink(runAndParseSingle("get-datacenter " + id), uriInfo.getRequestUriBuilder().build());
    }

    @Override
    public DataCenter update(UriInfo uriInfo, DataCenter datacenter) {
        StringBuilder buf = new StringBuilder();

        buf.append("$h = get-datacenter " + id + "\n");

        if (datacenter.getName() != null) {
            buf.append("$h.name = \"" + datacenter.getName() + "\"");
        }

        buf.append("\n");
        buf.append("update-datacenter -datacenterobject $v");

        return addLink(runAndParseSingle(buf.toString()), uriInfo.getRequestUriBuilder().build());
    }
}