package com.redhat.rhevm.api.model;

public enum StatisticUnit {

    NONE,
    PERCENT,
    BYTES,
    SECONDS,
    BYTES_PER_SECOND,
    BITS_PER_SECOND,
    COUNT_PER_SECOND;

    public String value() {
        return name().toLowerCase();
    }

    public static StatisticUnit fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
