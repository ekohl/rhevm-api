package com.redhat.rhevm.api.model;

public enum PowerManagementStatus {

    ON,
    OFF;

    public String value() {
        return name().toLowerCase();
    }

    public static PowerManagementStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
