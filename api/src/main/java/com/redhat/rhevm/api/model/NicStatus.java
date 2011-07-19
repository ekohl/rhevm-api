package com.redhat.rhevm.api.model;

public enum NicStatus {

    DOWN,
    UP;

    public String value() {
        return name().toLowerCase();
    }

    public static NicStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
