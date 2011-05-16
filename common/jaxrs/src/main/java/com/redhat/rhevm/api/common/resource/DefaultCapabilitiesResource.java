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
import com.redhat.rhevm.api.model.DiskFormat;
import com.redhat.rhevm.api.model.DiskFormats;
import com.redhat.rhevm.api.model.DiskInterface;
import com.redhat.rhevm.api.model.DiskInterfaces;
import com.redhat.rhevm.api.model.DiskType;
import com.redhat.rhevm.api.model.DiskTypes;
import com.redhat.rhevm.api.model.DisplayType;
import com.redhat.rhevm.api.model.DisplayTypes;
import com.redhat.rhevm.api.model.FenceType;
import com.redhat.rhevm.api.model.FenceTypes;
import com.redhat.rhevm.api.model.NicType;
import com.redhat.rhevm.api.model.NicTypes;
import com.redhat.rhevm.api.model.PowerManagement;
import com.redhat.rhevm.api.model.Option;
import com.redhat.rhevm.api.model.Options;
import com.redhat.rhevm.api.model.PowerManagers;
import com.redhat.rhevm.api.model.SchedulingPolicies;
import com.redhat.rhevm.api.model.SchedulingPolicyType;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageDomainTypes;
import com.redhat.rhevm.api.model.StorageType;
import com.redhat.rhevm.api.model.StorageTypes;
import com.redhat.rhevm.api.model.VersionCaps;
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

    private void addNicTypes(VersionCaps version, NicType... types) {
        version.setNicTypes(new NicTypes());
        for (NicType type : types) {
            version.getNicTypes().getNicTypes().add(type.value());
        }
    }

    {
        addNicTypes(VERSION21, NicType.values());
        addNicTypes(VERSION22, NicType.values());
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
