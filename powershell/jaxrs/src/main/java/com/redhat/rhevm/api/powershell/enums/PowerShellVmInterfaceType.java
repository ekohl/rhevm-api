/*
 * Copyright © 2011 Red Hat, Inc.
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
package com.redhat.rhevm.api.powershell.enums;

import java.util.HashMap;

import com.redhat.rhevm.api.model.NicInterface;

public enum PowerShellVmInterfaceType {
    rtl8139_pv(NicInterface.RTL8139_VIRTIO),
    rtl8139(NicInterface.RTL8139),
    e1000(NicInterface.E1000),
    pv(NicInterface.VIRTIO);

    private static HashMap<NicInterface, PowerShellVmInterfaceType> mapping;
    static {
        mapping = new HashMap<NicInterface, PowerShellVmInterfaceType>();
        for (PowerShellVmInterfaceType value : values()) {
            mapping.put(value.model, value);
        }
    }

    private NicInterface model;

    private PowerShellVmInterfaceType(NicInterface model) {
        this.model = model;
    }

    public NicInterface map() {
        return model;
    }

    public static PowerShellVmInterfaceType forModel(NicInterface model) {
        return mapping.get(model);
    }
}
