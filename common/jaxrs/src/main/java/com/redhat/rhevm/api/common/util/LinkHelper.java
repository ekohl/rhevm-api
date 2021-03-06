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
package com.redhat.rhevm.api.common.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.redhat.rhevm.api.model.ActionsBuilder;
import com.redhat.rhevm.api.model.BaseResource;
import com.redhat.rhevm.api.model.CdRom;
import com.redhat.rhevm.api.model.Domain;
import com.redhat.rhevm.api.model.Event;
import com.redhat.rhevm.api.model.Cluster;
import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.Disk;
import com.redhat.rhevm.api.model.HostNIC;
import com.redhat.rhevm.api.model.Host;
import com.redhat.rhevm.api.model.File;
import com.redhat.rhevm.api.model.Group;
import com.redhat.rhevm.api.model.Link;
import com.redhat.rhevm.api.model.Network;
import com.redhat.rhevm.api.model.NIC;
import com.redhat.rhevm.api.model.Permission;
import com.redhat.rhevm.api.model.Permit;
import com.redhat.rhevm.api.model.Role;
import com.redhat.rhevm.api.model.Snapshot;
import com.redhat.rhevm.api.model.Statistic;
import com.redhat.rhevm.api.model.Storage;
import com.redhat.rhevm.api.model.StorageDomain;
import com.redhat.rhevm.api.model.Tag;
import com.redhat.rhevm.api.model.Template;
import com.redhat.rhevm.api.model.User;
import com.redhat.rhevm.api.model.VmPool;
import com.redhat.rhevm.api.model.VM;
import com.redhat.rhevm.api.resource.AssignedNetworkResource;
import com.redhat.rhevm.api.resource.AssignedNetworksResource;
import com.redhat.rhevm.api.resource.AssignedTagResource;
import com.redhat.rhevm.api.resource.AssignedTagsResource;
import com.redhat.rhevm.api.resource.AttachedStorageDomainResource;
import com.redhat.rhevm.api.resource.AttachedStorageDomainsResource;
import com.redhat.rhevm.api.resource.ClusterResource;
import com.redhat.rhevm.api.resource.ClustersResource;
import com.redhat.rhevm.api.resource.DataCenterResource;
import com.redhat.rhevm.api.resource.DataCentersResource;
import com.redhat.rhevm.api.resource.DeviceResource;
import com.redhat.rhevm.api.resource.DevicesResource;
import com.redhat.rhevm.api.resource.DomainsResource;
import com.redhat.rhevm.api.resource.DomainResource;
import com.redhat.rhevm.api.resource.DiskResource;
import com.redhat.rhevm.api.resource.EventResource;
import com.redhat.rhevm.api.resource.EventsResource;
import com.redhat.rhevm.api.resource.ReadOnlyDeviceResource;
import com.redhat.rhevm.api.resource.ReadOnlyDevicesResource;
import com.redhat.rhevm.api.resource.RolesResource;
import com.redhat.rhevm.api.resource.HostResource;
import com.redhat.rhevm.api.resource.HostsResource;
import com.redhat.rhevm.api.resource.HostStorageResource;
import com.redhat.rhevm.api.resource.HostNicResource;
import com.redhat.rhevm.api.resource.HostNicsResource;
import com.redhat.rhevm.api.resource.FileResource;
import com.redhat.rhevm.api.resource.FilesResource;
import com.redhat.rhevm.api.resource.GroupResource;
import com.redhat.rhevm.api.resource.GroupsResource;
import com.redhat.rhevm.api.resource.NetworkResource;
import com.redhat.rhevm.api.resource.NetworksResource;
import com.redhat.rhevm.api.resource.NicResource;
import com.redhat.rhevm.api.resource.PermissionResource;
import com.redhat.rhevm.api.resource.AssignedPermissionsResource;
import com.redhat.rhevm.api.resource.PermitResource;
import com.redhat.rhevm.api.resource.PermitsResource;
import com.redhat.rhevm.api.resource.RoleResource;
import com.redhat.rhevm.api.resource.AssignedRolesResource;
import com.redhat.rhevm.api.resource.SnapshotResource;
import com.redhat.rhevm.api.resource.SnapshotsResource;
import com.redhat.rhevm.api.resource.StatisticResource;
import com.redhat.rhevm.api.resource.StatisticsResource;
import com.redhat.rhevm.api.resource.StorageResource;
import com.redhat.rhevm.api.resource.StorageDomainContentResource;
import com.redhat.rhevm.api.resource.StorageDomainContentsResource;
import com.redhat.rhevm.api.resource.StorageDomainResource;
import com.redhat.rhevm.api.resource.StorageDomainsResource;
import com.redhat.rhevm.api.resource.TagResource;
import com.redhat.rhevm.api.resource.TagsResource;
import com.redhat.rhevm.api.resource.TemplateResource;
import com.redhat.rhevm.api.resource.TemplatesResource;
import com.redhat.rhevm.api.resource.UserResource;
import com.redhat.rhevm.api.resource.UsersResource;
import com.redhat.rhevm.api.resource.VmPoolResource;
import com.redhat.rhevm.api.resource.VmPoolsResource;
import com.redhat.rhevm.api.resource.VmResource;
import com.redhat.rhevm.api.resource.VmsResource;
import com.redhat.rhevm.api.resource.DomainGroupResource;
import com.redhat.rhevm.api.resource.DomainGroupsResource;
import com.redhat.rhevm.api.resource.DomainUserResource;
import com.redhat.rhevm.api.resource.DomainUsersResource;

/**
 * Contains a static addLinks() method which constructs any href attributes
 * and action links required by a representation.
 *
 * The information used to build links is obtained from the annotations on
 * the API definition interfaces.

 * For example, a link to a VM is the combination of the @Path attribute on
 * VmsResource and the VM id - i.e. '/rhevm-api/vms/{vm_id}'
 *
 * Resource collections which are a sub-resource of a parent collection
 * present a more difficult challenge. For example, the link to a VM tag
 * is the combination of the @Path attribute on VmsResource, the VM id,
 * the @Path attribute on VmResource.getTagsResource() and the tag id -
 * i.e. '/rhevm-api/vms/{vm_id}/tags/{tag_id}'
 * In most cases the parent type may be computed, but in exceptional
 * cases there are a number of equally valid candidates. Disambiguation
 * is achieved via an explicit suggestedParentType parameter.
 *
 * To be able to do this we need, for each collection, the collection type
 * (e.g. AssignedTagsResource), the resource type (e.g. AssignedTagResource)
 * and the parent model type (e.g. VM). The TYPES map below is populated
 * with this information for every resource type.
 */
public class LinkHelper {

    private static final String SEARCH_RELATION = "/search";
    private static final String SEARCH_TEMPLATE = "?search={query}";

    /**
     * A constant representing the pseudo-parent of a top-level collection
     */
    private static final Class<? extends BaseResource> NO_PARENT = BaseResource.class;

    /**
     * A map describing every possible collection
     */
    private static ModelToCollectionsMap TYPES = new ModelToCollectionsMap();

    static {
        ParentToCollectionMap map;

        map = new ParentToCollectionMap(ReadOnlyDeviceResource.class, ReadOnlyDevicesResource.class, Template.class);
        TYPES.put(CdRom.class, map);

        map = new ParentToCollectionMap(DeviceResource.class, DevicesResource.class, VM.class);
        TYPES.put(CdRom.class, map);

        map = new ParentToCollectionMap(ClusterResource.class, ClustersResource.class);
        TYPES.put(Cluster.class, map);

        map = new ParentToCollectionMap(DataCenterResource.class, DataCentersResource.class);
        TYPES.put(DataCenter.class, map);

        map = new ParentToCollectionMap(ReadOnlyDeviceResource.class, ReadOnlyDevicesResource.class, Template.class);
        TYPES.put(Disk.class, map);

        map = new ParentToCollectionMap(DeviceResource.class, DevicesResource.class, VM.class);
        TYPES.put(Disk.class, map);

        map = new ParentToCollectionMap(DiskResource.class, DevicesResource.class, VM.class);
        TYPES.put(Disk.class, map);

        map = new ParentToCollectionMap(HostResource.class, HostsResource.class);
        TYPES.put(Host.class, map);

        map = new ParentToCollectionMap(HostNicResource.class, HostNicsResource.class, Host.class);
        TYPES.put(HostNIC.class, map);

        map = new ParentToCollectionMap(FileResource.class, FilesResource.class, StorageDomain.class);
        TYPES.put(File.class, map);

        map = new ParentToCollectionMap(GroupResource.class, GroupsResource.class);
        map.add(DomainGroupResource.class, DomainGroupsResource.class, Domain.class);
        TYPES.put(Group.class, map);

        map = new ParentToCollectionMap(PermissionResource.class, AssignedPermissionsResource.class, User.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Group.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, Role.class);
        map.add(PermissionResource.class, AssignedPermissionsResource.class, VM.class);
        TYPES.put(Permission.class, map);

        map = new ParentToCollectionMap(NetworkResource.class, NetworksResource.class);
        map.add(AssignedNetworkResource.class, AssignedNetworksResource.class, Cluster.class);
        TYPES.put(Network.class, map);

        map = new ParentToCollectionMap(ReadOnlyDeviceResource.class, ReadOnlyDevicesResource.class, Template.class);
        TYPES.put(NIC.class, map);

        map = new ParentToCollectionMap(DeviceResource.class, DevicesResource.class, VM.class);
        TYPES.put(NIC.class, map);

        map = new ParentToCollectionMap(NicResource.class, DevicesResource.class, VM.class);
        TYPES.put(NIC.class, map);

        map = new ParentToCollectionMap(PermitResource.class, PermitsResource.class, Role.class);
        TYPES.put(Permit.class, map);

        map = new ParentToCollectionMap(RoleResource.class, RolesResource.class);
        map.add(RoleResource.class, AssignedRolesResource.class, User.class);
        TYPES.put(Role.class, map);

        map = new ParentToCollectionMap(SnapshotResource.class, SnapshotsResource.class, VM.class);
        TYPES.put(Snapshot.class, map);

        map = new ParentToCollectionMap(StorageResource.class, HostStorageResource.class, Host.class);
        TYPES.put(Storage.class, map);

        map = new ParentToCollectionMap(StorageDomainResource.class, StorageDomainsResource.class);
        map.add(AttachedStorageDomainResource.class, AttachedStorageDomainsResource.class, DataCenter.class);
        TYPES.put(StorageDomain.class, map);

        map = new ParentToCollectionMap(TagResource.class, TagsResource.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Host.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, User.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, VM.class);
        map.add(AssignedTagResource.class, AssignedTagsResource.class, Group.class);
        TYPES.put(Tag.class, map);

        map = new ParentToCollectionMap(TemplateResource.class, TemplatesResource.class);
        map.add(StorageDomainContentResource.class, StorageDomainContentsResource.class, StorageDomain.class);
        TYPES.put(Template.class, map);

        map = new ParentToCollectionMap(UserResource.class, UsersResource.class);
        map.add(DomainUserResource.class, DomainUsersResource.class, Domain.class);
        TYPES.put(User.class, map);

        map = new ParentToCollectionMap(VmResource.class, VmsResource.class);
        map.add(StorageDomainContentResource.class, StorageDomainContentsResource.class, StorageDomain.class);
        TYPES.put(VM.class, map);

        map = new ParentToCollectionMap(VmPoolResource.class, VmPoolsResource.class);
        TYPES.put(VmPool.class, map);

        map = new ParentToCollectionMap(EventResource.class, EventsResource.class);
        TYPES.put(Event.class, map);

        map = new ParentToCollectionMap(DomainResource.class, DomainsResource.class);
        TYPES.put(Domain.class, map);

        map = new ParentToCollectionMap(StatisticResource.class, StatisticsResource.class, Disk.class);
        map.add(StatisticResource.class, StatisticsResource.class, Host.class);
        map.add(StatisticResource.class, StatisticsResource.class, HostNIC.class);
        map.add(StatisticResource.class, StatisticsResource.class, NIC.class);
        map.add(StatisticResource.class, StatisticsResource.class, VM.class);
        TYPES.put(Statistic.class, map);
    }

    /**
     * Obtain the relative path to a top-level collection
     *
     * The path is simply the value of the @Path annotation on the
     * supplied collection resource type
     *
     * @param clz the collection resource type
     * @return    the relative path to the collection
     */
    private static String getPath(Class<?> clz) {
        Path pathAnnotation = (Path)clz.getAnnotation(Path.class);

        String path = pathAnnotation.value();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    /**
     * Obtain the relative path to a sub-collection
     *
     * The path is obtained from the @Path annotation on the method on @parent
     * which returns an instance of @clz
     *
     * A case-insensitive check for @type's name as a substring of the method
     * is also performed to guard against the case where @parent has multiple
     * methods returning instances of @clz, e.g. VmResource has multiple
     * methods return DevicesResource instances
     *
     * @param clz    the collection resource type (e.g. AssignedTagsResource)
     * @param parent the parent resource type (e.g. VmResource)
     * @param type   the model type (e.g. Tag)
     * @return       the relative path to the collection
     */
    private static String getPath(Class<?> clz, Class<?> parent, Class<?> type) {
        for (Method method : parent.getMethods()) {
            if (method.getName().startsWith("get") &&
                clz.isAssignableFrom(method.getReturnType()) &&
                isPluralResourceGetter(method.getName(), type.getSimpleName())) {
                Path pathAnnotation = (Path)method.getAnnotation(Path.class);
                return pathAnnotation.value();
            }
        }
        return null;
    }

    private static boolean isPluralResourceGetter(String method, String type) {
        method = method.toLowerCase();
        type = type.toLowerCase();

        method = chopStart(method, "get");
        method = chopEnd(method, "resource");
        method = chopEnd(method, "s");

        if (type.endsWith("y")) {
            method = chopEnd(method, "ie");
            type = chopEnd(type, "y");
        }

        return method.contains(type);
    }

    private static String chopStart(String str, String chop) {
        if (str.startsWith(chop)) {
            return str.substring(chop.length());
        } else {
            return str;
        }
    }

    private static String chopEnd(String str, String chop) {
        if (str.endsWith(chop)) {
            return str.substring(0, str.length() - chop.length());
        } else {
            return str;
        }
    }

    /**
     * Obtain a set of inline BaseResource objects from @obj
     *
     * i.e. return the value of any properties on @obj which are a
     * sub-type of BaseResource
     *
     * @param obj the object to check
     * @return    a list of any inline BaseResource objects
     */
    private static List<BaseResource> getInlineResources(Object obj) {
        ArrayList<BaseResource> ret = new ArrayList<BaseResource>();

        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().startsWith("get") &&
                BaseResource.class.isAssignableFrom(method.getReturnType())) {
                try {
                    BaseResource inline = (BaseResource)method.invoke(obj);
                    if (inline != null) {
                        ret.add(inline);
                    }
                } catch (Exception e) {
                    // invocation target exception should not occur on simple getter
                }
            }
        }

        return ret;
    }

    /**
     * Unset the property on @model of type @type
     *
     * @param model the object with the property to unset
     * @param type  the type of the property
     */
    private static void unsetInlineResource(BaseResource model, Class<?> type) {
        for (Method method : model.getClass().getMethods()) {
            if (method.getName().startsWith("set")) {
                try {
                    if (type.isAssignableFrom(method.getParameterTypes()[0])) {
                        method.invoke(model, new Object[]{null});
                        return;
                    }
                } catch (Exception e) {
                    // invocation target exception should not occur on simple setter
                }
            }
        }
    }

    /**
     * Return any parent object set on @model
     *
     * i.e. return the value of any bean property whose type matches @parentType
     *
     * @param model      object to check
     * @param parentType the type of the parent
     * @return           the parent object, or null if not set
     */
    private static <R extends BaseResource> BaseResource getParentModel(R model, Class<?> parentType) {
        for (BaseResource inline : getInlineResources(model)) {
            if (parentType.isAssignableFrom(inline.getClass())) {
                return inline;
            }
        }
        return null;
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model the object to query for
     * @return      the #Collection instance representing the object's collection
     */
    private static Collection getCollection(BaseResource model) {
        return getCollection(model, null);
    }

    /**
     * Lookup the #Collection instance which represents this object
     *
     * i.e. for a VM tag (i.e. a Tag object which its VM property set)
     * return the #Collection instance which encapsulates AssignedTagResource,
     * AssignedTagsResource and VM.
     *
     * @param model                the object to query for
     * @param suggestedParentType  the suggested parent type
     * @return                     the #Collection instance representing the object's collection
     */
    private static Collection getCollection(BaseResource model, Class<? extends BaseResource> suggestedParentType) {
        ParentToCollectionMap collections = TYPES.get(model.getClass());

        if (suggestedParentType != null) {
            for (Class<? extends BaseResource> parentType : collections.keySet()) {
                if (parentType.equals(suggestedParentType)) {
                    return collections.get(parentType);
                }
            }
        }

        for (Class<? extends BaseResource> parentType : collections.keySet()) {
            if (parentType != NO_PARENT &&
                getParentModel(model, parentType) != null) {
                return collections.get(parentType);
            }
        }

        return collections.get(NO_PARENT);
    }

    /**
     * Create a #UriBuilder which encapsulates the path to an object
     *
     * i.e. for a VM tag, return a UriBuilder which encapsulates
     * '/rhevm-api/vms/{vm_id}/tags/{tag_id}'
     *
     * @param uriInfo the URI info
     * @param model   the object
     * @return        the #UriBuilder encapsulating the object's path
     */
    public static <R extends BaseResource> UriBuilder getUriBuilder(UriInfo uriInfo, R model) {
        return getUriBuilder(uriInfo, model, null);
    }

    /**
     * Create a #UriBuilder which encapsulates the path to an object
     *
     * i.e. for a VM tag, return a UriBuilder which encapsulates
     * '/rhevm-api/vms/{vm_id}/tags/{tag_id}'
     *
     * @param uriInfo              the URI info
     * @param model                the object
     * @param suggestedParentType  the suggested parent type
     * @return                     the #UriBuilder encapsulating the object's path
     */
    public static <R extends BaseResource> UriBuilder getUriBuilder(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        Collection collection = getCollection(model, suggestedParentType);
        if (collection == null) {
            return null;
        }

        UriBuilder uriBuilder;

        if (collection.getParentType() != NO_PARENT) {
            BaseResource parent = getParentModel(model, collection.getParentType());

            Collection parentCollection = getCollection(parent, suggestedParentType);

            String path = getPath(collection.getCollectionType(),
                                  parentCollection.getResourceType(),
                                  model.getClass());

            uriBuilder = getUriBuilder(uriInfo, parent).path(path);
        } else {
            String path = getPath(collection.getCollectionType());
            uriBuilder = uriInfo != null
                         ? UriBuilder.fromPath(uriInfo.getBaseUri().getPath()).path(path)
                         : UriBuilder.fromPath(path);
        }

        return uriBuilder.path(model.getId());
    }

    /**
     * Set the href attribute on the supplied object
     *
     * e.g. set href = '/rhevm-api/vms/{vm_id}/tags/{tag_id}' on a VM tag
     *
     * @param uriInfo  the URI info
     * @param model    the object
     * @return         the model, with the href attribute set
     */
    private static <R extends BaseResource> void setHref(UriInfo uriInfo, R model) {
        setHref(uriInfo, model, null);
    }

    /**
     * Set the href attribute on the supplied object
     *
     * e.g. set href = '/rhevm-api/vms/{vm_id}/tags/{tag_id}' on a VM tag
     *
     * @param uriInfo              the URI info
     * @param model                the object
     * @param suggestedParentType  the suggested parent type
     * @return                     the model, with the href attribute set
     */
    private static <R extends BaseResource> void setHref(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo, model, suggestedParentType);
        if (uriBuilder != null) {
            model.setHref(uriBuilder.build().toString());
        }
    }

    /**
     * Construct the set of action links for an object
     *
     * @param uriInfo the URI info
     * @param model   the object
     * @param suggestedParentType  the suggested parent type
     * @return        the object, including its set of action links
     */
    private static <R extends BaseResource> void setActions(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        Collection collection = getCollection(model);
        UriBuilder uriBuilder = getUriBuilder(uriInfo, model, suggestedParentType);
        if (uriBuilder != null) {
            ActionsBuilder actionsBuilder = new ActionsBuilder(uriBuilder, collection.getResourceType());
            model.setActions(actionsBuilder.build());
        }
    }

    /**
     * Set the href attribute on the object (and its inline objects)
     * and construct its set of action links
     *
     * @param uriInfo  the URI info
     * @param model    the object
     * @param suggestedParentType  the suggested parent type
     * @return         the object, with href attributes and action links
     */
    public static <R extends BaseResource> R addLinks(UriInfo uriInfo, R model) {
        return addLinks(uriInfo, model, null);
    }

    public static <R extends BaseResource> R addLinks(UriInfo uriInfo, R model, Class<? extends BaseResource> suggestedParentType) {
        setHref(uriInfo, model, suggestedParentType);
        setActions(uriInfo, model, suggestedParentType);

        for (BaseResource inline : getInlineResources(model)) {
            if (inline.getId() != null) {
                setHref(uriInfo, inline);
            }
            for (BaseResource grandParent : getInlineResources(inline)) {
                unsetInlineResource(inline, grandParent.getClass());
            }
        }

        return model;
    }

    /**
     * Appends searchable links to resource's Href
     *
     * @param url to append to
     * @param resource to add links to
     * @param rel link ro add
     * @param LinkFlags used to specify different link options
     */
    public static void addLink(BaseResource resource, String rel, LinkFlags flags) {
        addLink(resource.getHref(), resource, rel, flags);
    }

    /**
     * Adds searchable links to resource
     *
     * @param url to append to
     * @param resource to add links to
     * @param rel link ro add
     * @param LinkFlags used to specify different link options
     */
    public static void addLink(String url, BaseResource resource, String rel, LinkFlags flags) {
        Link link = new Link();
        link.setRel(rel);
        link.setHref(combine(url, rel));
        resource.getLinks().add(link);

        if (flags == LinkFlags.SEARCHABLE) {
            addLink(url, resource, rel);
        }
    }

    /**
     * Appends searchable links to resource's Href
     *
     * @param url to append to and combine search dialect
     * @param resource to add links to
     * @param rel link ro add
     */
    public static void addLink(BaseResource resource, String rel) {
        addLink(resource.getHref(), resource, rel);
    }

    /**
     * Adds searchable links to resource
     *
     * @param url to append to and combine search dialect
     * @param resource to add links to
     * @param rel link ro add
     */
    public static void addLink(String url, BaseResource resource, String rel) {
        Link link = new Link();
        link.setRel(rel + SEARCH_RELATION);
        link.setHref(combine(url, rel) + SEARCH_TEMPLATE);
        resource.getLinks().add(link);
    }

    /**
     * Combine head and tail portions of a URI path.
     *
     * @param head the path head
     * @param tail the path tail
     * @return the combined head and tail
     */
    public static String combine(String head, String tail) {
        if (head.endsWith("/")) {
            head = head.substring(0, head.length() - 1);
        }
        if (tail.startsWith("/")) {
            tail = tail.substring(1);
        }
        return head + "/" + tail;
    }

    /**
     * A #Map sub-class which maps a model type (e.g. Tag.class) to a
     * set of suitable collection definitions.
     */
    private static class ModelToCollectionsMap extends HashMap<Class<? extends BaseResource>, ParentToCollectionMap> { }

    /**
     * A #Map sub-class which maps a parent model type to collection
     * definition.
     *
     * e.g. the map for Tag contains a collection definition for the
     * describing the VM, Host and User tags sub-collections. It also
     * contains a collection definition describing the top-level
     * tags collection which is keyed on the NO_PARENT key.
     */
    private static class ParentToCollectionMap extends LinkedHashMap<Class<? extends BaseResource>, Collection> {
        public ParentToCollectionMap(Class<?> resourceType,
                                     Class<?> collectionType,
                                     Class<? extends BaseResource> parentType) {
            super();
            add(resourceType, collectionType, parentType);
        }

        public ParentToCollectionMap(Class<?> resourceType,
                                     Class<?> collectionType) {
            this(resourceType, collectionType, NO_PARENT);
        }

        public void add(Class<?> resourceType,
                        Class<?> collectionType,
                        Class<? extends BaseResource> parentType) {
            put(parentType, new Collection(resourceType, collectionType, parentType));
        }
    }

    /**
     * A description of a collection type, its resource type and the parent
     * resource which contains it, if any.
     *
     * e.g. for the VM tags collection, resourceType is AssignedTagResource,
     * collectionType is AssignedTagsResource and parentType is VM
     */
    private static class Collection {
        private final Class<?> resourceType;
        private final Class<?> collectionType;
        private final Class<?> parentType;

        public Collection(Class<?> resourceType, Class<?> collectionType, Class<?> parentType) {
            this.resourceType = resourceType;
            this.collectionType = collectionType;
            this.parentType = parentType;
        }

        public Class<?> getResourceType()      { return resourceType; }
        public Class<?> getCollectionType()    { return collectionType; }
        public Class<?> getParentType()        { return parentType; }
    }

    /**
     * Used to specify link options
     */
    public enum LinkFlags { NONE, SEARCHABLE; }
}
