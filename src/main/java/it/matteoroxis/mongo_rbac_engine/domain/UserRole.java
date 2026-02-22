package it.matteoroxis.mongo_rbac_engine.domain;

import java.util.Set;

public enum UserRole {

    CUSTOMER(Set.of(
            Permission.ORDER_CREATE,
            Permission.ORDER_CANCEL,
            Permission.ORDER_VIEW
    )),

    FINANCE(Set.of(
            Permission.ORDER_VIEW,
            Permission.REFUND_APPROVE
    )),

    ADMIN(Set.of(Permission.values()));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}
