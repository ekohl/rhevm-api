package com.redhat.rhevm.api.common.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

public class DetailHelper {

    private static final String ACCEPT = "Accept";
    private static final String DETAIL = "detail";
    private static final String PARAM_SEPARATOR = ";";
    private static final String VALUE_SEPARATOR = "=";
    private static final String DETAIL_SEPARATOR = "\\+";

    public static boolean include(HttpHeaders httpheaders, String relation) {
        List<String> accepts = httpheaders.getRequestHeader(ACCEPT);
        if (!(accepts == null || accepts.isEmpty())) {
            String[] parameters = accepts.get(0).split(PARAM_SEPARATOR);
            for (String parameter : parameters) {
                String[] includes = parameter.split(VALUE_SEPARATOR);
                if (includes.length > 1 && DETAIL.equalsIgnoreCase(includes[0].trim())) {
                    for (String rel : includes[1].trim().split(DETAIL_SEPARATOR)) {
                        if (relation.equalsIgnoreCase(rel.trim())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public enum Detail {
        DISKS,
        NICS,
        STATISTICS,
        TAGS
    }

    public static Set<Detail> getDetails(HttpHeaders httpHeaders) {
        Set<Detail> details = EnumSet.noneOf(Detail.class);
        for (Detail detail : Detail.class.getEnumConstants()) {
            if (include(httpHeaders, detail.name().toLowerCase())) {
                details.add(detail);
            }
        }
        return details;
    }
}
