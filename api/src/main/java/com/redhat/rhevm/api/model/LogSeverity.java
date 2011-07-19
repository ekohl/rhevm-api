package com.redhat.rhevm.api.model;

public enum LogSeverity {

    NORMAL,
    WARNING,
    ERROR,
    ALERT;

    public String value() {
        return name().toLowerCase();
    }

    public static LogSeverity fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
