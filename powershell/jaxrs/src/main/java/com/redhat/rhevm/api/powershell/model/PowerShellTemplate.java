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

import com.redhat.rhevm.api.common.util.TimeZoneMapping;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CpuTopology;
import com.redhat.rhevm.api.model.Display;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.Domain;
import com.redhat.rhevm.api.model.HighAvailability;
import com.redhat.rhevm.api.model.OperatingSystem;
import com.redhat.rhevm.api.model.Boot;
import com.redhat.rhevm.api.model.OsType;
import com.redhat.rhevm.api.model.Status;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.powershell.enums.PowerShellBootSequence;
import com.redhat.rhevm.api.powershell.enums.PowerShellOriginType;
import com.redhat.rhevm.api.powershell.enums.PowerShellVmTemplateStatus;
import com.redhat.rhevm.api.powershell.enums.PowerShellVmType;
import com.redhat.rhevm.api.powershell.util.PowerShellParser;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;

import static com.redhat.rhevm.api.powershell.util.PowerShellUtils.last;

public class PowerShellTemplate extends Template {

    private String cdIsoPath;
    public String getCdIsoPath() {
        return cdIsoPath;
    }
    public void setCdIsoPath(String cdIsoPath) {
        this.cdIsoPath = cdIsoPath;
    }

    private String taskIds;

    public String getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds;
    }

    public static List<PowerShellTemplate> parse(PowerShellParser parser, String output) {
        List<PowerShellTemplate> ret = new ArrayList<PowerShellTemplate>();

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
            } else if (PowerShellAsyncTask.isTask(entity)) {
                last(ret).setTaskIds(PowerShellAsyncTask.parseTask(entity, last(ret).getTaskIds()));
                continue;
            } else if (PowerShellAsyncTask.isStatus(entity)) {
                String creationStatus = last(ret).getCreationStatus();
                last(ret).setCreationStatus(PowerShellAsyncTask.parseStatus(entity, creationStatus==null ? null : Status.fromValue(creationStatus)).value());
                continue;
            }

            PowerShellTemplate template = new PowerShellTemplate();

            template.setId(entity.get("templateid"));
            template.setName(entity.get("name"));
            template.setDescription(entity.get("description"));
            template.setType(entity.get("vmtype", PowerShellVmType.class).map().value());
            template.setMemory(entity.get("memsizemb", Integer.class) * 1024L * 1024L);
            template.setCdIsoPath(entity.get("cdisopath"));
            template.setStatus(entity.get("status", PowerShellVmTemplateStatus.class).map().value());

            CpuTopology topo = new CpuTopology();
            topo.setSockets(entity.get("numofsockets", Integer.class));
            topo.setCores(entity.get("numofcpuspersocket", Integer.class));
            CPU cpu = new CPU();
            cpu.setTopology(topo);
            template.setCpu(cpu);

            OperatingSystem os = new OperatingSystem();
            OsType osType = PowerShellVM.parseOsType(entity.get("operatingsystem"));
            if (osType != null) {
                os.setType(osType.value());
            }
            for (Boot boot : entity.get("defaultbootsequence", PowerShellBootSequence.class).map()) {
                os.getBoot().add(boot);
            }
            template.setOs(os);

            template.setHighAvailability(new HighAvailability());
            template.getHighAvailability().setEnabled(entity.get("autostartup", Boolean.class));

            template.setStateless(entity.get("isstateless", Boolean.class));
            template.setTimezone(TimeZoneMapping.getJava(entity.get("timezone")));

            String domain = entity.get("domain");
            if (domain != null) {
                template.setDomain(new Domain());
                template.getDomain().setName(domain);
            }

            DisplayType displayType = PowerShellVM.parseDisplayType(entity.get("displaytype"));
            if (displayType != null) {
                Display display = new Display();
                display.setType(displayType.value());
                display.setMonitors(entity.get("numofmonitors", Integer.class));
                template.setDisplay(display);
            }

            Cluster cluster = new Cluster();
            cluster.setId(entity.get("hostclusterid", String.class, Integer.class).toString());
            template.setCluster(cluster);

            if (dates.containsKey(entity.get("creationdate"))) {
                template.setCreationTime(dates.get(entity.get("creationdate")));
            }

            template.setOrigin(entity.get("origin", PowerShellOriginType.class).map());

            ret.add(template);
        }

        return ret;
    }
}
