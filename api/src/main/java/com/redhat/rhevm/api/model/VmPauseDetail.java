package com.redhat.rhevm.api.model;

public enum VmPauseDetail {
    GENERAL_ERROR,
    IO_ERROR,
    NO_SPACE_ERROR,
    PERMISSIONS_DENIED_ERROR,
    NO_ERROR;

    public String value() {
        return name().toLowerCase();
    }

    public static VmPauseDetail fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
