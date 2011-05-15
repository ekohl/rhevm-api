package com.redhat.rhevm.api.resource;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.BaseResources;

@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_X_YAML})
public interface RemovableStorageDomainContentsResource<C extends BaseResources, R extends BaseResource> extends StorageDomainContentsResource<C, R> {

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") String id);
}
