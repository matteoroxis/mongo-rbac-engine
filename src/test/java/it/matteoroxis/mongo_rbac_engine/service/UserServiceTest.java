package it.matteoroxis.mongo_rbac_engine.service;
import it.matteoroxis.mongo_rbac_engine.document.UserDocument;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.domain.UserRole;
import it.matteoroxis.mongo_rbac_engine.dto.request.UserRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.UserResponse;
import it.matteoroxis.mongo_rbac_engine.exception.ForbiddenException;
import it.matteoroxis.mongo_rbac_engine.exception.UserNotFoundException;
import it.matteoroxis.mongo_rbac_engine.mapper.UserMapper;
import it.matteoroxis.mongo_rbac_engine.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private AuthorizationService authorizationService;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserService userService;
    private UserPrincipal adminPrincipal;
    private UserPrincipal customerPrincipal;
    private UserDocument sampleDoc;
    private UserResponse sampleResponse;
    @BeforeEach
    void setUp() {
        adminPrincipal = new UserPrincipal("admin-1", Set.of(UserRole.ADMIN));
        customerPrincipal = new UserPrincipal("c-1", Set.of(UserRole.CUSTOMER));
        sampleDoc = new UserDocument();
        sampleDoc.setId("u-1");
        sampleDoc.setEmail("test@example.com");
        sampleDoc.setRoles(Set.of("ADMIN"));
        sampleDoc.setStatus("ACTIVE");
        sampleResponse = new UserResponse("u-1", "test@example.com", Set.of("ADMIN"), "ACTIVE");
    }
    // ---- getAllUsers ----
    @Test
    @DisplayName("getAllUsers restituisce la lista mappata")
    void getAllUsers_returnsMappedList() {
        when(userRepository.findAll()).thenReturn(List.of(sampleDoc));
        when(userMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        List<UserResponse> result = userService.getAllUsers();
        assertThat(result).hasSize(1).first().isEqualTo(sampleResponse);
    }
    // ---- getUserById ----
    @Test
    @DisplayName("getUserById restituisce utente esistente")
    void getUserById_found() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(sampleDoc));
        when(userMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        UserResponse result = userService.getUserById("u-1");
        assertThat(result.getId()).isEqualTo("u-1");
    }
    @Test
    @DisplayName("getUserById lancia UserNotFoundException se non trovato")
    void getUserById_notFound_throws() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById("missing"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("missing");
    }
    // ---- createUser ----
    @Test
    @DisplayName("createUser con ADMIN salva e restituisce il nuovo utente")
    void createUser_admin_succeeds() {
        UserRequest req = new UserRequest();
        req.setEmail("new@example.com");
        req.setRoles(Set.of("CUSTOMER"));
        req.setStatus("ACTIVE");
        when(userMapper.toDocument(req)).thenReturn(sampleDoc);
        when(userRepository.save(sampleDoc)).thenReturn(sampleDoc);
        when(userMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        UserResponse result = userService.createUser(adminPrincipal, req);
        assertThat(result).isEqualTo(sampleResponse);
        verify(authorizationService).checkPermission(adminPrincipal, Permission.USER_MANAGE);
        verify(userRepository).save(sampleDoc);
    }
    @Test
    @DisplayName("createUser con CUSTOMER lancia ForbiddenException")
    void createUser_customer_forbidden() {
        doThrow(new ForbiddenException(Permission.USER_MANAGE))
                .when(authorizationService).checkPermission(customerPrincipal, Permission.USER_MANAGE);
        UserRequest req = new UserRequest();
        assertThatThrownBy(() -> userService.createUser(customerPrincipal, req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("USER_MANAGE");
        verify(userRepository, never()).save(any());
    }
    // ---- updateUser ----
    @Test
    @DisplayName("updateUser con ADMIN aggiorna i campi e salva")
    void updateUser_admin_succeeds() {
        UserRequest req = new UserRequest();
        req.setEmail("updated@example.com");
        req.setRoles(Set.of("FINANCE"));
        req.setStatus("INACTIVE");
        when(userRepository.findById("u-1")).thenReturn(Optional.of(sampleDoc));
        when(userRepository.save(sampleDoc)).thenReturn(sampleDoc);
        when(userMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        userService.updateUser(adminPrincipal, "u-1", req);
        verify(authorizationService).checkPermission(adminPrincipal, Permission.USER_MANAGE);
        verify(userRepository).save(sampleDoc);
        assertThat(sampleDoc.getEmail()).isEqualTo("updated@example.com");
    }
    @Test
    @DisplayName("updateUser con id inesistente lancia UserNotFoundException")
    void updateUser_notFound_throws() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.updateUser(adminPrincipal, "missing", new UserRequest()))
                .isInstanceOf(UserNotFoundException.class);
    }
    // ---- deleteUser ----
    @Test
    @DisplayName("deleteUser con ADMIN elimina l'utente")
    void deleteUser_admin_succeeds() {
        when(userRepository.existsById("u-1")).thenReturn(true);
        userService.deleteUser(adminPrincipal, "u-1");
        verify(authorizationService).checkPermission(adminPrincipal, Permission.USER_MANAGE);
        verify(userRepository).deleteById("u-1");
    }
    @Test
    @DisplayName("deleteUser con id inesistente lancia UserNotFoundException")
    void deleteUser_notFound_throws() {
        when(userRepository.existsById("missing")).thenReturn(false);
        assertThatThrownBy(() -> userService.deleteUser(adminPrincipal, "missing"))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).deleteById(any());
    }
}