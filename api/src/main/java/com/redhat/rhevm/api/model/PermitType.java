package com.redhat.rhevm.api.model;

public enum PermitType {
    CREATE_VM(RoleType.USER),
    DELETE_VM(RoleType.USER),
    EDIT_VM_PROPERTIES(RoleType.USER),
    VM_BASIC_OPERATIONS(RoleType.USER),
    CHANGE_VM_CD(RoleType.USER),
    MIGRATE_VM(RoleType.USER),
    CONNECT_TO_VM(RoleType.USER),
    IMPORT_EXPORT_VM(RoleType.ADMIN),
    CONFIGURE_VM_NETWORK(RoleType.USER),
    CONFIGURE_VM_STORAGE(RoleType.USER),
    MOVE_VM(RoleType.USER),
    MANIPULATE_VM_SNAPSHOTS(RoleType.USER),
    // host (vds) actions groups
    CREATE_HOST(RoleType.ADMIN),
    EDIT_HOST_CONFIGURATION(RoleType.ADMIN),
    DELETE_HOST(RoleType.ADMIN),
    MANIPUTLATE_HOST(RoleType.ADMIN),
    CONFIGURE_HOST_NETWORK(RoleType.ADMIN),
    // templates actions groups
    CREATE_TEMPLATE(RoleType.USER),
    EDIT_TEMPLATE_PROPERTIES(RoleType.USER),
    DELETE_TEMPLATE(RoleType.USER),
    COPY_TEMPLATE(RoleType.USER),
    CONFIGURE_TEMPLATE_NETWORK(RoleType.USER),
    // vm pools actions groups
    CREATE_VM_POOL(RoleType.USER),
    EDIT_VM_POOL_CONFIGURATION(RoleType.USER),
    DELETE_VM_POOL(RoleType.USER),
    VM_POOL_BASIC_OPERATIONS(RoleType.USER),
    // clusters actions groups
    CREATE_CLUSTER(RoleType.ADMIN),
    EDIT_CLUSTER_CONFIGURATION(RoleType.ADMIN),
    DELETE_CLUSTER(RoleType.ADMIN),
    CONFIGURE_CLUSTER_NETWORK(RoleType.ADMIN),
    // users and MLA actions groups
    MANIPULATE_USERS(RoleType.ADMIN),
    MANIPULATE_ROLES(RoleType.ADMIN),
    MANIPULATE_PERMISSIONS(RoleType.USER),
    // storage domains actions groups
    CREATE_STORAGE_DOMAIN(RoleType.ADMIN),
    EDIT_STORAGE_DOMAIN_CONFIGURATION(RoleType.ADMIN),
    DELETE_STORAGE_DOMAIN(RoleType.ADMIN),
    MANIPULATE_STORAGE_DOMAIN(RoleType.ADMIN),
    // storage pool actions groups
    CREATE_STORAGE_POOL(RoleType.ADMIN),
    DELETE_STORAGE_POOL(RoleType.ADMIN),
    EDIT_STORAGE_POOL_CONFIGURATION(RoleType.ADMIN),
    CONFIGURE_STORAGE_POOL_NETWORK(RoleType.ADMIN),

    // rhevm generic
    CONFIGURE_RHEVM(RoleType.ADMIN);

    private RoleType role;
    private PermitType(RoleType role) {
        this.role = role;
    }

    public String value() {
        return name().toLowerCase();
    }

    public RoleType getRole() {
        return role;
    }
}
