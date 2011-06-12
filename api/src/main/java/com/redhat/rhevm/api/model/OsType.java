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
package com.redhat.rhevm.api.model;

public enum OsType {
    UNASSIGNED,
    WINDOWS_XP,
    WINDOWS_2003,
    WINDOWS_2008,
    OTHER_LINUX,
    OTHER,
    RHEL_5,
    RHEL_4,
    RHEL_3,
    WINDOWS_2003x64,
    WINDOWS_7,
    WINDOWS_7x64,
    RHEL_5x64,
    RHEL_4x64,
    RHEL_3x64,
    WINDOWS_2008x64,
    WINDOWS_2008R2x64,
    RHEL_6,
    RHEL_6x64;

    public String value() {
        return name().toLowerCase();
    }

    public static OsType fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
