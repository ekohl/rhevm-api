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
package com.redhat.rhevm.api.powershell.resource;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.powershell.util.PowerShellUtils;


public class PowerShellStorageDomainsResource extends AbstractPowerShellCollectionResource<StorageDomain, PowerShellStorageDomainResource> implements StorageDomainsResource {

    /* Storage domains that have been torn down but not deleted
     * FIXME: this map shouldn't be static and needs synchronization
     */
    private static HashMap<String, PowerShellStorageDomainResource> tornDownDomains =
        new HashMap<String, PowerShellStorageDomainResource>();

    @Override
    public StorageDomains list(UriInfo uriInfo) {
        StorageDomains ret = new StorageDomains();

        List<StorageDomain> storageDomains = PowerShellStorageDomainResource.runAndParse(getShell(), "select-storagedomain", true);

        for (StorageDomain storageDomain : storageDomains) {
            ret.getStorageDomains().add(PowerShellStorageDomainResource.addLinks(storageDomain));
        }

        for (String id : tornDownDomains.keySet()) {
            PowerShellStorageDomainResource resource = tornDownDomains.get(id);
            ret.getStorageDomains().add(resource.addLinks(resource.getTornDown()));
        }

        return ret;
    }

    @Override
    public Response add(UriInfo uriInfo, StorageDomain storageDomain) {
        StringBuilder buf = new StringBuilder();

        String hostArg = null;
        if (storageDomain.getHost().isSetId()) {
            hostArg = PowerShellUtils.escape(storageDomain.getHost().getId());
        } else {
            buf.append("$h = select-host -searchtext ");
            buf.append(PowerShellUtils.escape("name=" +  storageDomain.getHost().getName()));
            buf.append("\n");
            hostArg = "$h.hostid";
        }

        buf.append("add-storagedomain");

        buf.append(" -name " + PowerShellUtils.escape(storageDomain.getName()));

        buf.append(" -hostid " + hostArg);

        buf.append(" -domaintype ");
        switch (storageDomain.getType()) {
        case DATA:
            buf.append("Data");
            break;
        case ISO:
            buf.append("ISO");
            break;
        case EXPORT:
            buf.append("Export");
            break;
        default:
            assert false : storageDomain.getType();
            break;
        }

        Storage storage = storageDomain.getStorage();

        buf.append(" -storagetype " + storage.getType().toString());
        buf.append(" -storage ");

        switch (storage.getType()) {
        case NFS:
            buf.append(PowerShellUtils.escape(storage.getAddress() + ":" + storage.getPath()));
            break;
        case ISCSI:
        case FCP:
        default:
            assert false : storage.getType();
            break;
        }

        storageDomain = PowerShellStorageDomainResource.runAndParseSingle(getShell(), buf.toString(), true);

        storageDomain = PowerShellStorageDomainResource.addLinks(storageDomain);

        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder().path(storageDomain.getId());

        return Response.created(uriBuilder.build()).entity(storageDomain).build();
    }

    @Override
    public void remove(String id) {
        tornDownDomains.remove(id);
        removeSubResource(id);
    }

    @Override
    public StorageDomainResource getStorageDomainSubResource(UriInfo uriInfo, String id) {
        if (tornDownDomains.containsKey(id)) {
            return tornDownDomains.get(id);
        } else {
            return getSubResource(id);
        }
    }

    protected PowerShellStorageDomainResource createSubResource(String id) {
        return new PowerShellStorageDomainResource(id, this, shellPools);
    }

    /**
     * Add a storage domain from the set of torn down storage domains.
     * <p>
     * This method should be called when a storage domain is torn down. At
     * this point it no longer exists in RHEV-M itself and just needs to
     * be removed using DELETE.
     *
     * @param id the RHEV-M ID of the StorageDomain
     * @param resource the PowerShellStorageDomainResource
     */
    public void addToTornDownDomains(String id, PowerShellStorageDomainResource resource) {
        tornDownDomains.put(id, resource);
    }

    /**
     * Build an absolute URI for a given storage domain
     *
     * @param baseUriBuilder a UriBuilder representing the base URI
     * @param id the storage domain ID
     * @return an absolute URI
     */
    public static String getHref(UriBuilder baseUriBuilder, String id) {
        return baseUriBuilder.clone().path("storagedomains").path(id).build().toString();
    }
}
