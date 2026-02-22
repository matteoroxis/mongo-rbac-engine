package it.matteoroxis.mongo_rbac_engine.exception;


import it.matteoroxis.mongo_rbac_engine.domain.Permission;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(Permission permission) {
        super("Missing permission: " + permission.name());
    }
}
