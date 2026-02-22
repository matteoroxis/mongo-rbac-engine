package it.matteoroxis.mongo_rbac_engine.domain;

import java.util.Set;

public class UserPrincipal {

    private final String id;
    private final Set<UserRole> roles;

    public UserPrincipal(String id, Set<UserRole> roles) {
        this.id = id;
        this.roles = roles;
    }

    public boolean hasPermission(Permission permission) {
        return roles.stream()
                .flatMap(role -> role.permissions().stream())
                .anyMatch(p -> p == permission);
    }

    public String id() {
        return id;
    }
}
