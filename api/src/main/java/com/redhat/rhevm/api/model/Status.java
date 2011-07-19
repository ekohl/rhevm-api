
package com.redhat.rhevm.api.model;

public enum Status {

    PENDING,
    IN_PROGRESS,
    COMPLETE,
    FAILED;

    public String value() {
        return name().toLowerCase();
    }

    public static Status fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
