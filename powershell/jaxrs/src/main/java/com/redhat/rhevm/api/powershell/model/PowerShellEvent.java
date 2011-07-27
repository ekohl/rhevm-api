/*
 * Copyright © 2010 Red Hat, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.rhevm.api.powershell.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;

import com.redhat.rhevm.api.model.Event;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.powershell.enums.PowerShellLogSeverity;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
import com.redhat.rhevm.api.powershell.util.UUID;

public class PowerShellEvent {

    public static List<Event> parse(PowerShellParser parser, String output) {
        List<Event> ret = new ArrayList<Event>();

        Map<String, XMLGregorianCalendar> dates = new HashMap<String, XMLGregorianCalendar>();
        String date = null;

        for (PowerShellParser.Entity entity : parser.parse(output)) {
            if (PowerShellParser.DATE_TYPE.equals(entity.getType())) {
                date = entity.getValue();
                continue;
            } else if (PowerShellParser.STRING_TYPE.equals(entity.getType())) {
                dates.put(date, PowerShellUtils.parseDate(entity.getValue()));
                date = null;
                continue;
            }

            ret.add(parseEvent(entity, dates));
        }

        return ret;
    }

    private static Event parseEvent(PowerShellParser.Entity entity, Map<String, XMLGregorianCalendar> dates) {
        Event event = new Event();

        event.setId(Long.toString(entity.get("id", Long.class)));
        event.setDescription(entity.get("message"));
        event.setCode(entity.get("logtype", Integer.class));
        event.setSeverity(entity.get("severity", PowerShellLogSeverity.class).map().value());

        if (dates.containsKey(entity.get("logtime"))) {
            event.setTime(dates.get(entity.get("logtime")));
        }

        if (entity.isSet("userid") && !entity.get("userid").equals(UUID.EMPTY)) {
            User user = new User();
            user.setId(entity.get("userid"));
            event.setUser(user);
        }
        if (entity.isSet("vmid") && !entity.get("vmid").equals(UUID.EMPTY)) {
            VM vm = new VM();
            vm.setId(entity.get("vmid"));
            event.setVm(vm);
        }
        if (entity.isSet("storagedomainid") && !entity.get("storagedomainid").equals(UUID.EMPTY)) {
            StorageDomain sd = new StorageDomain();
            sd.setId(entity.get("storagedomainid"));
            event.setStorageDomain(sd);
        }
        if (entity.isSet("hostid")) {
            Host host = new Host();
            host.setId(entity.get("hostid", Integer.class).toString());
            event.setHost(host);
        }
        if (entity.isSet("vmtemplateid") && !entity.get("vmtemplateid").equals(UUID.EMPTY)) {
            Template template = new Template();
            template.setId(entity.get("vmtemplateid"));
            event.setTemplate(template);
        }

        return event;
    }
}
