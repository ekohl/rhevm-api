package com.redhat.rhevm.api.model;

public enum ValueType {

    INTEGER,
    DECIMAL;

    public String value() {
        return name().toLowerCase();
    }

    public static ValueType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
