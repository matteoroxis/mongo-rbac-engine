package it.matteoroxis.mongo_rbac_engine.controller;

import it.matteoroxis.mongo_rbac_engine.config.SecurityConfig;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.domain.UserRole;
import it.matteoroxis.mongo_rbac_engine.dto.response.OrderResponse;
import it.matteoroxis.mongo_rbac_engine.exception.ForbiddenException;
import it.matteoroxis.mongo_rbac_engine.exception.GlobalExceptionHandler;
import it.matteoroxis.mongo_rbac_engine.exception.OrderNotFoundException;
import it.matteoroxis.mongo_rbac_engine.exception.UnauthorizedException;
import it.matteoroxis.mongo_rbac_engine.resolver.UserPrincipalResolver;
import it.matteoroxis.mongo_rbac_engine.service.OrderService;
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

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "jwt.secret=4d6f6e676f52626163456e67696e655365637265744b657932303236212121",
        "jwt.expiration-ms=86400000"
})
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OrderService orderService;
    @MockitoBean private UserPrincipalResolver resolver;
    @MockitoBean private it.matteoroxis.mongo_rbac_engine.security.JwtService jwtService;
    @MockitoBean private it.matteoroxis.mongo_rbac_engine.repository.UserRepository userRepository;

    private UserPrincipal customerPrincipal;
    private OrderResponse sampleResponse;

    @BeforeEach
    void setUp() {
        customerPrincipal = new UserPrincipal("c-1", Set.of(UserRole.CUSTOMER));
        sampleResponse = new OrderResponse("o-1", "c-1", "test order", "PENDING", Instant.now());
    }

    @Test
    @DisplayName("GET /orders -> 200")
    void getAll_returns200() throws Exception {
        when(resolver.resolve(any())).thenReturn(customerPrincipal);
        when(orderService.getAllOrders(customerPrincipal)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/orders").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("o-1"));
    }

    @Test
    @DisplayName("GET /orders without token -> 401")
    void getAll_missingToken_returns401() throws Exception {
        when(resolver.resolve(any())).thenThrow(new UnauthorizedException("Missing header"));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /orders/{id} -> 200")
    void getById_returns200() throws Exception {
        when(resolver.resolve(any())).thenReturn(customerPrincipal);
        when(orderService.getOrderById(customerPrincipal, "o-1")).thenReturn(sampleResponse);

        mockMvc.perform(get("/orders/o-1").header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("o-1"));
    }

    @Test
    @DisplayName("GET /orders/{id} not found -> 404")
    void getById_notFound_returns404() throws Exception {
        when(resolver.resolve(any())).thenReturn(customerPrincipal);
        when(orderService.getOrderById(any(), eq("missing")))
                .thenThrow(new OrderNotFoundException("missing"));

        mockMvc.perform(get("/orders/missing").header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /orders with ORDER_CREATE -> 201")
    void create_returns201() throws Exception {
        when(resolver.resolve(any())).thenReturn(customerPrincipal);
        when(orderService.createOrder(eq(customerPrincipal), any())).thenReturn(sampleResponse);

        String body = "{\"userId\":\"c-1\",\"description\":\"test order\"}";

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("o-1"));
    }

    @Test
    @DisplayName("POST /orders without permission -> 403")
    void create_forbidden_returns403() throws Exception {
        when(resolver.resolve(any())).thenReturn(customerPrincipal);
        when(orderService.createOrder(any(), any()))
                .thenThrow(new ForbiddenException(Permission.ORDER_CREATE));

        String body = "{\"userId\":\"c-1\",\"description\":\"order\"}";

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /orders/{id} -> 204")
    void cancel_returns204() throws Exception {
        when(resolver.resolve(any())).thenReturn(customerPrincipal);
        doNothing().when(orderService).cancelOrder(customerPrincipal, "o-1");

        mockMvc.perform(delete("/orders/o-1").header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /orders/{id} without permission -> 403")
    void cancel_forbidden_returns403() throws Exception {
        when(resolver.resolve(any())).thenReturn(customerPrincipal);
        doThrow(new ForbiddenException(Permission.ORDER_CANCEL))
                .when(orderService).cancelOrder(any(), eq("o-1"));

        mockMvc.perform(delete("/orders/o-1").header("Authorization", "Bearer token"))
                .andExpect(status().isForbidden());
    }
}