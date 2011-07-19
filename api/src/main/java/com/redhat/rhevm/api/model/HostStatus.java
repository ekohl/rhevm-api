package com.redhat.rhevm.api.model;

public enum HostStatus {

    DOWN,
    ERROR,
    INITIALIZING,
    INSTALLING,
    INSTALL_FAILED,
    MAINTENANCE,
    NON_OPERATIONAL,
    NON_RESPONSIVE,
    PENDING_APPROVAL,
    PREPARING_FOR_MAINTENANCE,
    PROBLEMATIC,
    REBOOT,
    UNASSIGNED,
    UP;

    public String value() {
        return name().toLowerCase();
    }

    public static HostStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
