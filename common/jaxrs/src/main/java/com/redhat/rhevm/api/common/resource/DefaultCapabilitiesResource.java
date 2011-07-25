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
package com.redhat.rhevm.api.common.resource;

import com.redhat.rhevm.api.model.BootDevice;
import com.redhat.rhevm.api.model.BootDevices;
import com.redhat.rhevm.api.model.Capabilities;
import com.redhat.rhevm.api.model.CPU;
import com.redhat.rhevm.api.model.CPUs;
import com.redhat.rhevm.api.model.CreationStates;
import com.redhat.rhevm.api.model.CreationStatus;
import com.redhat.rhevm.api.model.DataCenterStates;
import com.redhat.rhevm.api.model.DataCenterStatus;
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskFormats;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskInterfaces;
import com.redhat.rhevm.api.model.DiskStates;
import com.redhat.rhevm.api.model.DiskStatus;
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.DiskTypes;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.DisplayTypes;
import com.redhat.rhevm.api.model.FenceType;
import com.redhat.rhevm.api.model.FenceTypes;
import com.redhat.rhevm.api.model.NicInterface;
import com.redhat.rhevm.api.model.NicInterfaces;
import com.redhat.rhevm.api.model.HostNICStates;
import com.redhat.rhevm.api.model.HostStates;
import com.redhat.rhevm.api.model.HostStatus;
import com.redhat.rhevm.api.model.NetworkStates;
import com.redhat.rhevm.api.model.NetworkStatus;
import com.redhat.rhevm.api.model.NicStatus;
import com.redhat.rhevm.api.model.OsType;
import com.redhat.rhevm.api.model.OsTypes;
import com.redhat.rhevm.api.model.PowerManagement;
import com.redhat.rhevm.api.model.Option;
import com.redhat.rhevm.api.model.Options;
import com.redhat.rhevm.api.model.PowerManagementStates;
import com.redhat.rhevm.api.model.PowerManagementStatus;
import com.redhat.rhevm.api.model.PowerManagers;
import com.redhat.rhevm.api.model.SchedulingPolicies;
import com.redhat.rhevm.api.model.SchedulingPolicyType;
import com.redhat.rhevm.api.model.StorageDomainStates;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageDomainTypes;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.model.StorageTypes;
import com.redhat.rhevm.api.model.TemplateStates;
import com.redhat.rhevm.api.model.TemplateStatus;
import com.redhat.rhevm.api.model.VersionCaps;
import com.redhat.rhevm.api.model.VmStates;
import com.redhat.rhevm.api.model.VmStatus;
import com.redhat.rhevm.api.model.VmType;
import com.redhat.rhevm.api.model.VmTypes;
import com.redhat.rhevm.api.resource.CapabilitiesResource;

public class DefaultCapabilitiesResource implements CapabilitiesResource {

    private final VersionCaps VERSION22 = buildVersion(2, 2, true);
    private final VersionCaps VERSION21 = buildVersion(2, 1, false);

    private VersionCaps buildVersion(int major, int minor, boolean current) {
        VersionCaps version = new VersionCaps();
        version.setMajor(major);
        version.setMinor(minor);
        version.setCPUs(new CPUs());
        version.setCurrent(current);
        return version;
    }

    {
        addCpu(VERSION21, "Intel Xeon w/o XD/NX",  2);
        addCpu(VERSION21, "Intel Xeon",            3);
        addCpu(VERSION22, "Intel Xeon Core2",      4);
        addCpu(VERSION22, "Intel Xeon 45nm Core2", 5);
        addCpu(VERSION22, "Intel Xeon Core i7",    6);

        addCpu(VERSION21, "AMD Opteron G1 w/o NX", 2);
        addCpu(VERSION21, "AMD Opteron G1",        3);
        addCpu(VERSION22, "AMD Opteron G2",        4);
        addCpu(VERSION22, "AMD Opteron G3",        5);
    }

    private void addCpu(VersionCaps version, String id, int level) {
        CPU cpu = new CPU();

        cpu.setId(id);
        cpu.setLevel(level);

        version.getCPUs().getCPUs().add(cpu);

        if (version == VERSION21) {
            addCpu(VERSION22, id, level);
        }
    }

    {
        addPowerManager("alom", "secure=bool,port=int");
        addPowerManager("apc", "secure=bool,port=int,slot=int");
        addPowerManager("bladecenter", "secure=bool,port=int,slot=int");
        addPowerManager("drac5", "secure=bool,port=int");
        addPowerManager("eps", "slot=int");
        addPowerManager("ilo", "secure=bool,port=int");
        addPowerManager("ipmilan", "");
        addPowerManager("rsa", "secure=bool,port=int");
        addPowerManager("rsb", "");
        addPowerManager("wti", "secure=bool,port=int,slot=int");
    }

    private void addPowerManager(String type, String options) {
        addPowerManager(VERSION21, type, options);
        addPowerManager(VERSION22, type, options);
    }

    private void addPowerManager(VersionCaps version, String type, String options) {
        PowerManagement powerManagement = new PowerManagement();

        powerManagement.setType(type);

        powerManagement.setOptions(new Options());

        String[] opts = options.split(",");
        for (int i = 0; i < opts.length; i++) {
            if (opts[i].isEmpty()) {
                continue;
            }

            String[] parts = opts[i].split("=");

            Option option = new Option();
            option.setName(parts[0]);
            option.setType(parts[1]);
            powerManagement.getOptions().getOptions().add(option);
        }

        if (!version.isSetPowerManagers()) {
            version.setPowerManagers(new PowerManagers());
        }
        version.getPowerManagers().getPowerManagers().add(powerManagement);
    }

    private void addVmTypes(VersionCaps version, VmType... types) {
        version.setVmTypes(new VmTypes());
        for (VmType type : types) {
            version.getVmTypes().getVmTypes().add(type.value());
        }
    }

    {
        addVmTypes(VERSION21, VmType.values());
        addVmTypes(VERSION22, VmType.values());
    }

    private void addStorageTypes(VersionCaps version, StorageType... types) {
        version.setStorageTypes(new StorageTypes());
        for (StorageType type : types) {
            version.getStorageTypes().getStorageTypes().add(type.value());
        }
    }

    {
        addStorageTypes(VERSION21, StorageType.ISCSI, StorageType.FCP, StorageType.NFS);
        addStorageTypes(VERSION22, StorageType.ISCSI, StorageType.FCP, StorageType.NFS);
    }

    private void addOsTypes(VersionCaps version, OsType... types) {
        version.setOsTypes(new OsTypes());
        for (OsType type : types) {
            version.getOsTypes().getOsTypes().add(type.value());
        }
    }

    {
        addOsTypes(VERSION21, OsType.values());
        addOsTypes(VERSION22, OsType.values());
    }

    private void addStorageDomainTypes(VersionCaps version, StorageDomainType... types) {
        version.setStorageDomainTypes(new StorageDomainTypes());
        for (StorageDomainType type : types) {
            version.getStorageDomainTypes().getStorageDomainTypes().add(type.value());
        }
    }

    {
        addStorageDomainTypes(VERSION21, StorageDomainType.values());
        addStorageDomainTypes(VERSION22, StorageDomainType.values());
    }

    private void addFenceTypes(VersionCaps version, FenceType... types) {
        version.setFenceTypes(new FenceTypes());
        for (FenceType type : types) {
            version.getFenceTypes().getFenceTypes().add(type.value());
        }
    }

    {
        addFenceTypes(VERSION21, FenceType.values());
        addFenceTypes(VERSION22, FenceType.values());
    }

    private void addBootDevices(VersionCaps version, BootDevice... devs) {
        version.setBootDevices(new BootDevices());
        for (BootDevice dev : devs) {
            version.getBootDevices().getBootDevices().add(dev.value());
        }
    }

    {
        addBootDevices(VERSION21, BootDevice.values());
        addBootDevices(VERSION22, BootDevice.values());
    }

    private void addDisplayTypes(VersionCaps version, DisplayType... types) {
        version.setDisplayTypes(new DisplayTypes());
        for (DisplayType type : types) {
            version.getDisplayTypes().getDisplayTypes().add(type.value());
        }
    }

    {
        addDisplayTypes(VERSION21, DisplayType.values());
        addDisplayTypes(VERSION22, DisplayType.values());
    }

    private void addNicInterfaces(VersionCaps version, NicInterface... types) {
        version.setNicInterfaces(new NicInterfaces());
        for (NicInterface type : types) {
            version.getNicInterfaces().getNicInterfaces().add(type.value());
        }
    }

    {
        addNicInterfaces(VERSION21, NicInterface.values());
        addNicInterfaces(VERSION22, NicInterface.values());
    }

    private void addDiskTypes(VersionCaps version, DiskType... types) {
        version.setDiskTypes(new DiskTypes());
        for (DiskType type : types) {
            version.getDiskTypes().getDiskTypes().add(type.value());
        }
    }

    {
        addDiskTypes(VERSION21, DiskType.values());
        addDiskTypes(VERSION22, DiskType.values());
    }

    private void addDiskFormats(VersionCaps version, DiskFormat... types) {
        version.setDiskFormats(new DiskFormats());
        for (DiskFormat type : types) {
            version.getDiskFormats().getDiskFormats().add(type.value());
        }
    }

    {
        addDiskFormats(VERSION21, DiskFormat.values());
        addDiskFormats(VERSION22, DiskFormat.values());
    }

    private void addDiskInterfaces(VersionCaps version, DiskInterface... interfaces) {
        version.setDiskInterfaces(new DiskInterfaces());
        for (DiskInterface iface : interfaces) {
            version.getDiskInterfaces().getDiskInterfaces().add(iface.value());
        }
    }

    {
        addDiskInterfaces(VERSION21, DiskInterface.values());
        addDiskInterfaces(VERSION22, DiskInterface.values());
    }

    private void addCreationStates(VersionCaps version, CreationStatus... statuses) {
        version.setCreationStates(new CreationStates());
        for (CreationStatus status : statuses) {
            version.getCreationStates().getCreationStates().add(status.value());
        }
    }

    {
        addCreationStates(VERSION21, CreationStatus.values());
        addCreationStates(VERSION22, CreationStatus.values());
    }

    private void addPowerManagementStates(VersionCaps version, PowerManagementStatus... statuses) {
        version.setPowerManagementStates(new PowerManagementStates());
        for (PowerManagementStatus status : statuses) {
            version.getPowerManagementStates().getPowerManagementStates().add(status.value());
        }
    }

    {
        addPowerManagementStates(VERSION21, PowerManagementStatus.values());
        addPowerManagementStates(VERSION22, PowerManagementStatus.values());
    }

    private void addHostStates(VersionCaps version, HostStatus... statuses) {
        version.setHostStates(new HostStates());
        for (HostStatus status : statuses) {
            version.getHostStates().getHostStates().add(status.value());
        }
    }

    {
        addHostStates(VERSION21, HostStatus.values());
        addHostStates(VERSION22, HostStatus.values());
    }

    private void addNetworkStates(VersionCaps version, NetworkStatus... statuses) {
        version.setNetworkStates(new NetworkStates());
        for (NetworkStatus status : statuses) {
            version.getNetworkStates().getNetworkStates().add(status.value());
        }
    }

    {
        addNetworkStates(VERSION21, NetworkStatus.values());
        addNetworkStates(VERSION22, NetworkStatus.values());
    }

    private void addStorageDomainStates(VersionCaps version, StorageDomainStatus... statuses) {
        version.setStorageDomainStates(new StorageDomainStates());
        for (StorageDomainStatus status : statuses) {
            version.getStorageDomainStates().getStorageDomainStates().add(status.value());
        }
    }

    {
        addStorageDomainStates(VERSION21, StorageDomainStatus.values());
        addStorageDomainStates(VERSION22, StorageDomainStatus.values());
    }

    private void addTemplateStates(VersionCaps version, TemplateStatus... statuses) {
        version.setTemplateStates(new TemplateStates());
        for (TemplateStatus status : statuses) {
            version.getTemplateStates().getTemplateStates().add(status.value());
        }
    }

    {
        addTemplateStates(VERSION21, TemplateStatus.values());
        addTemplateStates(VERSION22, TemplateStatus.values());
    }

    private void addVmStates(VersionCaps version, VmStatus... statuses) {
        version.setVmStates(new VmStates());
        for (VmStatus status : statuses) {
            version.getVmStates().getVmStates().add(status.value());
        }
    }

    {
        addVmStates(VERSION21, VmStatus.values());
        addVmStates(VERSION22, VmStatus.values());
    }

    private void addDiskStates(VersionCaps version, DiskStatus... statuses) {
        version.setDiskStates(new DiskStates());
        for (DiskStatus status : statuses) {
            version.getDiskStates().getDiskStates().add(status.value());
        }
    }

    {
        addDiskStates(VERSION21, DiskStatus.values());
        addDiskStates(VERSION22, DiskStatus.values());
    }

    private void addDataCenterStates(VersionCaps version, DataCenterStatus... statuses) {
        version.setDataCenterStates(new DataCenterStates());
        for (DataCenterStatus status : statuses) {
            version.getDataCenterStates().getDataCenterStates().add(status.value());
        }
    }

    {
        addDataCenterStates(VERSION21, DataCenterStatus.values());
        addDataCenterStates(VERSION22, DataCenterStatus.values());
    }

    private void addHostNicStates(VersionCaps version, NicStatus... statuses) {
        version.setHostNicStates(new HostNICStates());
        for (NicStatus status : statuses) {
            version.getHostNicStates().getHostNICStates().add(status.value());
        }
    }

    {
        addHostNicStates(VERSION21, NicStatus.values());
        addHostNicStates(VERSION22, NicStatus.values());
    }

    private final SchedulingPolicies SCHEDULING_POLICIES = new SchedulingPolicies();

    {
        for (SchedulingPolicyType policy : SchedulingPolicyType.values()) {
            SCHEDULING_POLICIES.getPolicy().add(policy.value());
        }
    }

    public Capabilities get() {
        Capabilities caps = new Capabilities();
        caps.getVersions().add(VERSION22);
        caps.getVersions().add(VERSION21);
        caps.setSchedulingPolicies(SCHEDULING_POLICIES);
        return caps;
    }
}
