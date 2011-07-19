package com.redhat.rhevm.api.model;

public enum TemplateStatus {

    ILLEGAL,
    LOCKED,
    OK;

    public String value() {
        return name().toLowerCase();
    }

    public static TemplateStatus fromValue(String v) {
        try {
            return valueOf(v.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
