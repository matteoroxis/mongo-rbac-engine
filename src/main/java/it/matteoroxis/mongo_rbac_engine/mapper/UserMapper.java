package it.matteoroxis.mongo_rbac_engine.mapper;
import it.matteoroxis.mongo_rbac_engine.document.UserDocument;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.domain.UserRole;
import it.matteoroxis.mongo_rbac_engine.dto.request.UserRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.UserResponse;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;
@Component
public class UserMapper {
    public static UserPrincipal toPrincipal(UserDocument document) {
        Set<UserRole> roles = document.getRoles().stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
        return new UserPrincipal(document.getId(), roles);
    }
    public UserDocument toDocument(UserRequest request) {
        UserDocument doc = new UserDocument();
        doc.setEmail(request.getEmail());
        doc.setRoles(request.getRoles());
        doc.setStatus(request.getStatus());
        return doc;
    }
    public UserResponse toResponse(UserDocument doc) {
        return new UserResponse(doc.getId(), doc.getEmail(), doc.getRoles(), doc.getStatus());
    }
}