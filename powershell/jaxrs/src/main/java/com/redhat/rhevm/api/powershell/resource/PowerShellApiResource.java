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

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.redhat.rhevm.api.common.util.JAXBHelper;
import com.redhat.rhevm.api.common.util.LinkHelper;
import com.redhat.rhevm.api.common.util.LinkHelper.LinkFlags;
import com.redhat.rhevm.api.model.API;
import com.redhat.rhevm.api.model.ApiSummary;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.Hosts;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.LinkHeader;
import com.redhat.rhevm.api.model.ObjectFactory;
import com.redhat.rhevm.api.model.SpecialObjects;
import com.redhat.rhevm.api.model.StorageDomains;
import com.redhat.rhevm.api.model.SystemVersion;
import com.redhat.rhevm.api.model.Users;
import com.redhat.rhevm.api.model.VMs;
import com.redhat.rhevm.api.powershell.model.PowerShellSystemStats;
import com.redhat.rhevm.api.powershell.model.PowerShellSystemVersion;
import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
import com.redhat.rhevm.api.resource.ApiResource;

public class PowerShellApiResource
    extends InjectableUriProviderBase
    implements ApiResource {

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private API addLinks(API api) {
        addLink(api, "capabilities");
        addLink(api, "clusters", LinkFlags.SEARCHABLE);
        addLink(api, "datacenters", LinkFlags.SEARCHABLE);
        addLink(api, "domains");
        addLink(api, "events", LinkFlags.SEARCHABLE);
        addLink(api, "hosts", LinkFlags.SEARCHABLE);
        addLink(api, "networks"); // powershell has no select-network command
        addLink(api, "roles");
        addLink(api, "storagedomains", LinkFlags.SEARCHABLE);
        addLink(api, "tags");
        addLink(api, "templates", LinkFlags.SEARCHABLE);
        addLink(api, "users", LinkFlags.SEARCHABLE);
        addLink(api, "vmpools", LinkFlags.SEARCHABLE);
        addLink(api, "vms", LinkFlags.SEARCHABLE);

        addStaticLinks(getSpecialObjects(api).getLinks(),
                       new String[]{"templates/blank", "tags/root"},
                       new String[]{getTemplateBlankUri(), getTagRootUri()});

        return api;
    }

    private BaseResource getSpecialObjects(API api) {
        api.setSpecialObjects(new SpecialObjects());
        return api.getSpecialObjects();
    }

    private String getTagRootUri() {
        return LinkHelper.combine(getUriInfo().getBaseUri().getPath(), "tags/-1");
    }

    private String getTemplateBlankUri() {
        return LinkHelper.combine(getUriInfo().getBaseUri().getPath(), "templates/00000000-0000-0000-0000-000000000000");
    }

    private void addStaticLinks(List<Link> linker, String[] rels, String[] refs) {
        if(rels.length == refs.length){
            for(int i = 0; i < rels.length; i++){
                Link link = new Link();
                link.setRel(rels[i]);
                link.setHref(refs[i]);
                linker.add(link);
            }
        }
    }

    private void addLink(BaseResource api, String rel, LinkFlags flags) {
        LinkHelper.addLink(getUriInfo().getBaseUri().getPath(),api, rel, flags);
    }

    private void addLink(API api, String rel) {
        addLink(api, rel, LinkFlags.NONE);
    }

    private String addPath(UriBuilder uriBuilder, Link link) {
        String query = "";
        String path = relative(link);

        // otherwise UriBuilder.build() will substitute {query}
        if (path.contains("?")) {
            query = path.substring(path.indexOf("?"));
            path = path.substring(0, path.indexOf("?"));
        }

        link = JAXBHelper.clone(OBJECT_FACTORY.createLink(link));
        link.setHref(uriBuilder.clone().path(path).build() + query);

        return LinkHeader.format(link);
    }

    private void addHeader(API api, Response.ResponseBuilder responseBuilder, UriBuilder uriBuilder) {
        // concantenate links in a single header with a comma-separated value,
        // which is the canonical form according to:
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
        //
        StringBuffer header = new StringBuffer();

        for (Link l : api.getLinks()) {
            header.append(addPath(uriBuilder, l)).append(",");
        }

        header.setLength(header.length() - 1);

        responseBuilder.header("Link", header);
    }

    private Response.ResponseBuilder getResponseBuilder(API api) {
        UriBuilder uriBuilder = getUriInfo().getBaseUriBuilder();

        Response.ResponseBuilder responseBuilder = Response.ok();

        addHeader(api, responseBuilder, uriBuilder);

        return responseBuilder;
    }

    @Override
    public Response head() {
        API api = addLinks(new API());
        return getResponseBuilder(api).build();
    }

    @Override
    public Response get() {
        API api = addSummary(addSystemVersion(addLinks(new API())));
        return getResponseBuilder(api).entity(api).build();
    }

    private SystemVersion runAndParseSystemVersion(String command) {
        List<SystemVersion> versions = PowerShellSystemVersion.parse(getParser(),
                                                                     PowerShellCmd.runCommand(getPool(), command));
        return !versions.isEmpty() ? versions.get(0) : null;
    }

    private API addSystemVersion(API api) {
        api.setSystemVersion(runAndParseSystemVersion("get-version"));
        return api;
    }

    private PowerShellSystemStats runAndParseSummary(String command) {
        return PowerShellSystemStats.parse(getParser(), PowerShellCmd.runCommand(getPool(), command));
    }

    private API addSummary(API api) {
        PowerShellSystemStats stats = runAndParseSummary("get-systemstatistics");

        ApiSummary summary = new ApiSummary();

        summary.setVMs(new VMs());
        summary.getVMs().setTotal(stats.totalVms);
        summary.getVMs().setActive(stats.activeVms);

        summary.setHosts(new Hosts());
        summary.getHosts().setTotal(stats.totalHosts);
        summary.getHosts().setActive(stats.activeHosts);

        summary.setUsers(new Users());
        summary.getUsers().setTotal(stats.totalUsers);
        summary.getUsers().setActive(stats.activeUsers);

        summary.setStorageDomains(new StorageDomains());
        summary.getStorageDomains().setTotal(stats.totalStorageDomains);
        summary.getStorageDomains().setActive(stats.activeStorageDomains);

        api.setSummary(summary);

        return api;
    }

    private String relative(Link link) {
        return link.getHref().substring(link.getHref().indexOf(link.getRel().split("/")[0]), link.getHref().length());
    }
}
