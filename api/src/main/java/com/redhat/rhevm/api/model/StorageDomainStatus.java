package com.redhat.rhevm.api.model;

public enum StorageDomainStatus {

    ACTIVE,
    INACTIVE,
    LOCKED,
    MIXED,
    UNATTACHED,
    UNKNOWN;

    public String value() {
        return name().toLowerCase();
    }

    public static StorageDomainStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
