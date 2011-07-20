
package com.redhat.rhevm.api.model;

public enum CreationStatus {

    PENDING,
    IN_PROGRESS,
    COMPLETE,
    FAILED;

    public String value() {
        return name().toLowerCase();
    }

    public static CreationStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
