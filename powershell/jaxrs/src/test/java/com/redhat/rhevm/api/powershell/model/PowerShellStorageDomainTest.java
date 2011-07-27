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

import org.junit.Test;

import java.util.List;

import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomainStatus;
import com.redhat.rhevm.api.model.StorageDomainType;
import com.redhat.rhevm.api.model.StorageType;


public class PowerShellStorageDomainTest extends PowerShellModelTest {

    private static final long GB = 1024L * 1024L * 1024L;

    private void testStorageDomain(PowerShellStorageDomain s, String id, String name, StorageDomainType type, boolean isMaster, StorageDomainStatus status, Object available, Object used, Object committed) {
        assertEquals(id, s.getId());
        assertEquals(name, s.getName());
        assertEquals(type.value(), s.getType());
        assertEquals(isMaster, s.isMaster());
        assertEquals(status==null ? StorageDomainStatus.UNKNOWN.value() : status.value(), s.getStatus());
        assertEquals(available, s.getAvailable());
        assertEquals(used, s.getUsed());
        assertEquals(committed, s.getCommitted());
    }

    private void testNfsStorageDomain(PowerShellStorageDomain s, String id, String name, StorageDomainType type, boolean isMaster, StorageDomainStatus status, String address, String path, Object available, Object used, Object committed) {
        testStorageDomain(s, id, name, type, isMaster, status, available, used, committed);

        Storage storage = s.getStorage();
        assertNotNull(storage);

        assertEquals(StorageType.NFS.value(), storage.getType());
        assertEquals(address, storage.getAddress());
        assertEquals(path, storage.getPath());
    }

    private void testIscsiStorageDomain(PowerShellStorageDomain s, String id, String name, StorageDomainType type, boolean isMaster, StorageDomainStatus status, Object available, Object used, Object committed) {
        testStorageDomain(s, id, name, type, isMaster, status, available, used, committed);

        Storage storage = s.getStorage();
        assertNotNull(storage);

        assertEquals(StorageType.ISCSI.value(), storage.getType());
    }

    @Test
    public void testParse() throws Exception {
        String data = readFileContents("storagedomain.xml");
        assertNotNull(data);

        List<PowerShellStorageDomain> storageDomains = PowerShellStorageDomain.parse(getParser(), data);

        assertEquals(storageDomains.size(), 4);

        testNfsStorageDomain(storageDomains.get(0),
                             "788cd1e6-aa2b-412e-bcaf-0b37b96f00b2", "foo222",
                             StorageDomainType.DATA, false,  null,
                             "172.31.0.6", "/exports/RHEVX/markmc/images/2",
                             Long.valueOf(150L * GB), Long.valueOf(139L * GB), Long.valueOf(0L));

        testNfsStorageDomain(storageDomains.get(1),
                             "85fa8ff2-b3e4-483a-970d-7a8a13bc839f", "images0",
                             StorageDomainType.DATA, true, StorageDomainStatus.ACTIVE,
                             "172.31.0.6", "/exports/RHEVX/markmc/images/0",
                             Long.valueOf(150L * GB), Long.valueOf(139L * GB), Long.valueOf(16L * GB));

        testNfsStorageDomain(storageDomains.get(2),
                             "4fa5e14f-8404-4dee-a16f-172279376f0c", "iso0",
                             StorageDomainType.ISO, false, StorageDomainStatus.INACTIVE,
                             "172.31.0.6", "/exports/RHEVX/markmc/iso/0",
                             null, null, Long.valueOf(0L));

        testIscsiStorageDomain(storageDomains.get(3),
                               "746afd39-0229-4686-8ee6-7c1900848d00", "iscsi252",
                               StorageDomainType.DATA, false, StorageDomainStatus.UNATTACHED,
                               Long.valueOf(16L * GB), Long.valueOf(3L * GB), Long.valueOf(0L));
    }
}
