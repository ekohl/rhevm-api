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

public enum HostType {
    RHEL("rhel"), RHEV_H("rhev-h");

    private String value;

    HostType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static HostType fromValue(String v) {
        try {
            if (v==null) {
                return null;
            }
            if (v.equals("rhel")) {
                return RHEL;
            } else if (v.equals("rhev-h")) {
                return RHEV_H;
            } else {
                return valueOf(v.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
