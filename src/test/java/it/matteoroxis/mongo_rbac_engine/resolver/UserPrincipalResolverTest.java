package it.matteoroxis.mongo_rbac_engine.resolver;
import it.matteoroxis.mongo_rbac_engine.document.UserDocument;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.exception.UnauthorizedException;
import it.matteoroxis.mongo_rbac_engine.repository.UserRepository;
import it.matteoroxis.mongo_rbac_engine.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserPrincipalResolverTest {
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private HttpServletRequest httpRequest;
    @InjectMocks private UserPrincipalResolver resolver;
    private UserDocument sampleDoc;
    @BeforeEach
    void setUp() {
        sampleDoc = new UserDocument();
        sampleDoc.setId("u-1");
        sampleDoc.setEmail("test@example.com");
        sampleDoc.setRoles(Set.of("ADMIN"));
        sampleDoc.setStatus("ACTIVE");
    }
    @Test
    @DisplayName("resolve con token valido restituisce UserPrincipal")
    void resolve_validToken_returnsUserPrincipal() {
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUserId("valid.jwt.token")).thenReturn("u-1");
        when(userRepository.findById("u-1")).thenReturn(Optional.of(sampleDoc));
        UserPrincipal result = resolver.resolve(httpRequest);
        assertThat(result.id()).isEqualTo("u-1");
    }
    @Test
    @DisplayName("resolve senza header lancia UnauthorizedException")
    void resolve_missingHeader_throws() {
        when(httpRequest.getHeader("Authorization")).thenReturn(null);
        assertThatThrownBy(() -> resolver.resolve(httpRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authorization");
    }
    @Test
    @DisplayName("resolve con header senza prefisso Bearer lancia UnauthorizedException")
    void resolve_noBearerPrefix_throws() {
        when(httpRequest.getHeader("Authorization")).thenReturn("Basic somevalue");
        assertThatThrownBy(() -> resolver.resolve(httpRequest))
                .isInstanceOf(UnauthorizedException.class);
    }
    @Test
    @DisplayName("resolve con token non valido lancia UnauthorizedException")
    void resolve_invalidToken_throws() {
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(jwtService.extractUserId("bad.token")).thenThrow(new RuntimeException("bad signature"));
        assertThatThrownBy(() -> resolver.resolve(httpRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid or expired");
    }
    @Test
    @DisplayName("resolve con userId non trovato in DB lancia UnauthorizedException")
    void resolve_userNotFound_throws() {
        when(httpRequest.getHeader("Authorization")).thenReturn("Bearer valid.jwt.token");
        when(jwtService.extractUserId("valid.jwt.token")).thenReturn("ghost-id");
        when(userRepository.findById("ghost-id")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> resolver.resolve(httpRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("ghost-id");
    }
}