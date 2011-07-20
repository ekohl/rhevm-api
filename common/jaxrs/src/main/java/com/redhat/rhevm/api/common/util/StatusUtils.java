package com.redhat.rhevm.api.common.util;

import com.redhat.rhevm.api.model.CreationStatus;
import com.redhat.rhevm.api.model.Status;

public class StatusUtils {

    public static Status create(String statusStr) {
        if (statusStr==null) {
            return null;
        } else {
            Status status = new Status();
            status.setState(statusStr.toLowerCase());
            return status;
        }
    }

    public static<E extends Enum<E>> Status create(E statusEnum) {
        if (statusEnum==null) {
            return null;
        } else {
            return create(statusEnum.name());
        }
    }

    public static <E extends Enum<E>> boolean exists(Class<E> enumClass, String enumValue) {
        return getEnumValue(enumClass, enumValue)!=null;
    }

    public static<E extends Enum<E>> E getEnumValue(Class<E> enumClass, String enumValue) {
        try {
            return Enum.<E> valueOf(enumClass, enumValue);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static CreationStatus getRequestStatus(Status status) {
        return status==null ? null : getRequestStatus(status.getState());
    }

    public static CreationStatus getRequestStatus(String status) {
        if ( (status==null) || (status.isEmpty()) ) {
            return null;
        } else {
            return getEnumValue(CreationStatus.class, status);
        }
    }
}
