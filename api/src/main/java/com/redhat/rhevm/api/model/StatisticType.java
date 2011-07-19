package com.redhat.rhevm.api.model;

public enum StatisticType {

    GAUGE,
    COUNTER;

    public String value() {
        return name().toLowerCase();
    }

    public static StatisticType fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
