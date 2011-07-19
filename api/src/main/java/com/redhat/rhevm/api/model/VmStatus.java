package com.redhat.rhevm.api.model;

public enum VmStatus {

    UNASSIGNED,
    DOWN,
    UP,
    POWERING_UP,
    POWERED_DOWN,
    PAUSED,
    MIGRATING_FROM,
    MIGRATING_TO,
    UNKNOWN,
    NOT_RESPONDING,
    WAIT_FOR_LAUNCH,
    REBOOT_IN_PROGRESS,
    SAVING_STATE,
    RESTORING_STATE,
    SUSPENDED,
    IMAGE_ILLEGAL,
    IMAGE_LOCKED,
    POWERING_DOWN;

    public String value() {
        return name().toLowerCase();
    }

    public static VmStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
