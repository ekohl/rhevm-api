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
package com.redhat.rhevm.api.powershell.enums;

import java.util.HashMap;

public class EnumMapper {

    private final HashMap<String, Class<? extends Enum<?>>> mapping;

    {
        mapping = new HashMap<String, Class<? extends Enum<?>>>();
        mapping.put("RhevmCmd.HostSelectionAlgorithm",                      PowerShellHostSelectionAlgorithm.class);
        mapping.put("VdcDAL.AuditLogSeverity",                              PowerShellLogSeverity.class);
        mapping.put("VdcDAL.BootSequence",                                  PowerShellBootSequence.class);
        mapping.put("VdcDAL.DiskType",                                      PowerShellDiskType.class);
        mapping.put("VdcDAL.HypervisorType",                                PowerShellHypervisorType.class);
        mapping.put("VdcDAL.ImageStatus",                                   PowerShellImageStatus.class);
        mapping.put("VdcDAL.NetworkStatus",                                 PowerShellNetworkStatus.class);
        mapping.put("VdcDAL.OperationMode",                                 PowerShellOperationMode.class);
        mapping.put("VdcDAL.OriginType",                                    PowerShellOriginType.class);
        mapping.put("VdcDAL.PropagateErrors",                               PowerShellPropagateErrors.class);
        mapping.put("VdcDAL.StorageDomainSharedStatus",                     PowerShellStorageDomainSharedStatus.class);
        mapping.put("VdcDAL.StorageDomainStatus",                           PowerShellStorageDomainStatus.class);
        mapping.put("VdcDAL.StorageType",                                   PowerShellStorageType.class);
        mapping.put("VdcDAL.UsbPolicy",                                     PowerShellUsbPolicy.class);
        mapping.put("VdcDAL.VdsSpmStatus",                                  PowerShellVdsSpmStatus.class);
        mapping.put("VdcDAL.VmTemplateStatus",                              PowerShellVmTemplateStatus.class);
        mapping.put("VdcDAL.VmType",                                        PowerShellVmType.class);
        mapping.put("VdcDAL.VmOsType",                                      PowerShellOsType.class);
        mapping.put("VdcDAL.VolumeFormat",                                  PowerShellVolumeFormat.class);
        mapping.put("VdcDAL.VolumeType",                                    PowerShellVolumeType.class);
        mapping.put("VdcCommon.AsyncTasks.AsyncTaskStatusEnum",             PowerShellAsyncTaskStatus.class);
        mapping.put("VdcCommon.AsyncTasks.AsyncTaskResultEnum",             PowerShellAsyncTaskResult.class);
    };

    public boolean isEnum(String type) {
        return mapping.containsKey(type);
    }

    public Enum<?> parseEnum(String type, Integer value) {
        Class<? extends Enum<?>> clz = mapping.get(type);
        Class<?>[] paramTypes = new Class<?>[] { int.class };
        try {
            return clz.cast(clz.getMethod("forValue", paramTypes).invoke(null, value));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing " + clz + " enum", e);
        }
    }
}
