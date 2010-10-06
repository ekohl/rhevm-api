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

import javax.ws.rs.Produces;

import com.redhat.rhevm.api.common.resource.UriInfoProvider;
import com.redhat.rhevm.api.model.BaseDevice;
import com.redhat.rhevm.api.model.BaseDevices;
import com.redhat.rhevm.api.resource.MediaType;

import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
import com.redhat.rhevm.api.resource.ReadOnlyDevicesResource;

@Produces(MediaType.APPLICATION_XML)
public abstract class AbstractPowerShellDevicesResource<D extends BaseDevice, C extends BaseDevices>
    extends UriProviderWrapper
    implements ReadOnlyDevicesResource<D, C> {

    protected String parentId;

    public AbstractPowerShellDevicesResource(String parentId,
                                             PowerShellPoolMap shellPools,
                                             PowerShellParser parser,
                                             UriInfoProvider uriProvider) {
        super(null, shellPools, parser, uriProvider);
        this.parentId = parentId;
    }

    public AbstractPowerShellDevicesResource(String parentId, PowerShellPoolMap shellPools, UriInfoProvider uriProvider) {
        this(parentId, shellPools, null, uriProvider);
    }

    public abstract D addLinks(D device);

    public abstract List<D> getDevices();
}
