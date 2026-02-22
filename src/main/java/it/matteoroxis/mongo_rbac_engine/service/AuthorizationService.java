package it.matteoroxis.mongo_rbac_engine.service;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.exception.ForbiddenException;
import org.springframework.stereotype.Service;
@Service
public class AuthorizationService {
    public void checkPermission(UserPrincipal user, Permission permission) {
        if (!user.hasPermission(permission)) {
            throw new ForbiddenException(permission);
        }
    }
}