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
package com.redhat.rhevm.api.powershell.enums;

import java.util.HashMap;

import com.redhat.rhevm.api.model.OsType;

public enum PowerShellOsType {

    Unassigned(OsType.UNASSIGNED),
    WindowsXP(OsType.WINDOWS_XP),
    Windows2003(OsType.WINDOWS_2003),
    Windows2008(OsType.WINDOWS_2008),
    OtherLinux(OsType.OTHER_LINUX),
    Other(OsType.OTHER),
    RHEL5(OsType.RHEL_5),
    RHEL4(OsType.RHEL_4),
    RHEL3(OsType.RHEL_3),
    Windows2003x64(OsType.WINDOWS_2003x64),
    Windows7(OsType.WINDOWS_7),
    Windows7x64(OsType.WINDOWS_7x64),
    RHEL5x64(OsType.RHEL_5x64),
    RHEL4x64(OsType.RHEL_4x64),
    RHEL3x64(OsType.RHEL_3x64),
    Windows2008x64(OsType.WINDOWS_2008x64),
    Windows2008R2x64(OsType.WINDOWS_2008R2x64),
    RHEL6(OsType.RHEL_6),
    RHEL6x64(OsType.RHEL_6x64);

    private static HashMap<OsType, PowerShellOsType> mapping;
    static {
        mapping = new HashMap<OsType, PowerShellOsType>();
        for (PowerShellOsType value : values()) {
            mapping.put(value.model, value);
        }
    }

    private OsType model;

    private PowerShellOsType(OsType model) {
        this.model = model;
    }

    public OsType map() {
        return model;
    }

    public static PowerShellOsType forModel(OsType model) {
        return mapping.get(model);
    }
}
