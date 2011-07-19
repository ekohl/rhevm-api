package com.redhat.rhevm.api.model;

public enum DiskStatus {

    ILLEGAL,
    INVALID,
    LOCKED,
    OK;

    public String value() {
        return name().toLowerCase();
    }

    public static DiskStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
