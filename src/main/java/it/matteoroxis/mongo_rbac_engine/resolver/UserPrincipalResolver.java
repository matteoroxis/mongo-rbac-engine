package it.matteoroxis.mongo_rbac_engine.resolver;
import it.matteoroxis.mongo_rbac_engine.document.UserDocument;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.exception.UnauthorizedException;
import it.matteoroxis.mongo_rbac_engine.mapper.UserMapper;
import it.matteoroxis.mongo_rbac_engine.repository.UserRepository;
import it.matteoroxis.mongo_rbac_engine.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
@Component
public class UserPrincipalResolver {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final UserRepository userRepository;
    private final JwtService jwtService;
    public UserPrincipalResolver(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }
    /**
     * Extracts the JWT from the "Authorization: Bearer <token>" header,
     * verifies its signature and loads the corresponding UserPrincipal.
     */
    public UserPrincipal resolve(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("Missing or invalid Authorization header. Expected: Bearer <token>");
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        String userId;
        try {
            userId = jwtService.extractUserId(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid or expired JWT token: " + e.getMessage());
        }
        UserDocument doc = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found for id: " + userId));
        return UserMapper.toPrincipal(doc);
    }
}