package com.redhat.rhevm.api.model;

public enum StorageFormat {
    V1, V2;

    public String value() {
        return name().toLowerCase();
    }

    public static StorageFormat fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
