package com.redhat.rhevm.api.model;

public enum DataCenterStatus {

    UNINITIALIZED,
    UP,
    MAINTENANCE,
    NOT_OPERATIONAL,
    PROBLEMATIC,
    CONTEND;

    public String value() {
        return name().toLowerCase();
    }

    public static DataCenterStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
