package com.redhat.rhevm.api.model;

public enum NetworkStatus {

    OPERATIONAL,
    NON_OPERATIONAL;

    public String value() {
        return name().toLowerCase();
    }

    public static NetworkStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
