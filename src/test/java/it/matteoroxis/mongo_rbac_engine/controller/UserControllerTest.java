package it.matteoroxis.mongo_rbac_engine.controller;

import it.matteoroxis.mongo_rbac_engine.config.SecurityConfig;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.domain.UserRole;
import it.matteoroxis.mongo_rbac_engine.dto.response.UserResponse;
import it.matteoroxis.mongo_rbac_engine.exception.ForbiddenException;
import it.matteoroxis.mongo_rbac_engine.exception.GlobalExceptionHandler;
import it.matteoroxis.mongo_rbac_engine.exception.UnauthorizedException;
import it.matteoroxis.mongo_rbac_engine.exception.UserNotFoundException;
import it.matteoroxis.mongo_rbac_engine.repository.UserRepository;
import it.matteoroxis.mongo_rbac_engine.resolver.UserPrincipalResolver;
import it.matteoroxis.mongo_rbac_engine.security.JwtService;
import it.matteoroxis.mongo_rbac_engine.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "jwt.secret=4d6f6e676f52626163456e67696e655365637265744b657932303236212121",
        "jwt.expiration-ms=86400000"
})
class UserControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private UserService userService;
    @MockitoBean private UserPrincipalResolver resolver;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;

    private UserPrincipal adminPrincipal;
    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        adminPrincipal = new UserPrincipal("admin-1", Set.of(UserRole.ADMIN));
        sampleResponse = new UserResponse("u-1", "test@example.com", Set.of("ADMIN"), "ACTIVE");
    }

    @Test
    @DisplayName("GET /users -> 200 with user list")
    void getAll_returns200() throws Exception {
        when(resolver.resolve(any())).thenReturn(adminPrincipal);
        when(userService.getAllUsers()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/users").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("u-1"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /users without token -> 401")
    void getAll_missingToken_returns401() throws Exception {
        when(resolver.resolve(any())).thenThrow(new UnauthorizedException("Missing Authorization header"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users/{id} -> 200")
    void getById_returns200() throws Exception {
        when(resolver.resolve(any())).thenReturn(adminPrincipal);
        when(userService.getUserById("u-1")).thenReturn(sampleResponse);

        mockMvc.perform(get("/users/u-1").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("u-1"));
    }

    @Test
    @DisplayName("GET /users/{id} not found -> 404")
    void getById_notFound_returns404() throws Exception {
        when(resolver.resolve(any())).thenReturn(adminPrincipal);
        when(userService.getUserById("missing")).thenThrow(new UserNotFoundException("missing"));

        mockMvc.perform(get("/users/missing").header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users with ADMIN -> 201")
    void create_admin_returns201() throws Exception {
        when(resolver.resolve(any())).thenReturn(adminPrincipal);
        when(userService.createUser(eq(adminPrincipal), any())).thenReturn(sampleResponse);

        String body = "{\"email\":\"test@example.com\",\"roles\":[\"ADMIN\"],\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u-1"));
    }

    @Test
    @DisplayName("POST /users without permission -> 403")
    void create_forbidden_returns403() throws Exception {
        when(resolver.resolve(any())).thenReturn(adminPrincipal);
        when(userService.createUser(any(), any()))
                .thenThrow(new ForbiddenException(Permission.USER_MANAGE));

        String body = "{\"email\":\"x@x.com\",\"roles\":[\"CUSTOMER\"],\"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /users with invalid body -> 400")
    void create_invalidBody_returns400() throws Exception {
        when(resolver.resolve(any())).thenReturn(adminPrincipal);

        String body = "{\"email\":\"not-an-email\",\"roles\":[],\"status\":\"\"}";

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /users/{id} with ADMIN -> 204")
    void delete_admin_returns204() throws Exception {
        when(resolver.resolve(any())).thenReturn(adminPrincipal);
        doNothing().when(userService).deleteUser(adminPrincipal, "u-1");

        mockMvc.perform(delete("/users/u-1").header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }
}