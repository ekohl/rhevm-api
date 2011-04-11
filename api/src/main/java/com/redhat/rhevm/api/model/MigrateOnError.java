package com.redhat.rhevm.api.model;

public enum MigrateOnError {

    MIGRATE,
    DO_NOT_MIGRATE,
    MIGRATE_HIGHLY_AVAILABLE;

    public String value() {
        return name().toLowerCase();
    }

    public static MigrateOnError fromValue(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
