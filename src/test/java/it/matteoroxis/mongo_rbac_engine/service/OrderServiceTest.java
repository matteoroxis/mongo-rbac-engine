package it.matteoroxis.mongo_rbac_engine.service;
import it.matteoroxis.mongo_rbac_engine.document.OrderDocument;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.domain.UserRole;
import it.matteoroxis.mongo_rbac_engine.dto.request.OrderRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.OrderResponse;
import it.matteoroxis.mongo_rbac_engine.exception.ForbiddenException;
import it.matteoroxis.mongo_rbac_engine.exception.OrderNotFoundException;
import it.matteoroxis.mongo_rbac_engine.mapper.OrderMapper;
import it.matteoroxis.mongo_rbac_engine.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private AuthorizationService authorizationService;
    @Mock private OrderMapper orderMapper;
    @InjectMocks private OrderService orderService;
    private UserPrincipal customer;
    private UserPrincipal finance;
    private OrderDocument sampleDoc;
    private OrderResponse sampleResponse;
    @BeforeEach
    void setUp() {
        customer = new UserPrincipal("c-1", Set.of(UserRole.CUSTOMER));
        finance  = new UserPrincipal("f-1", Set.of(UserRole.FINANCE));
        sampleDoc = new OrderDocument();
        sampleDoc.setId("o-1");
        sampleDoc.setUserId("c-1");
        sampleDoc.setDescription("test order");
        sampleDoc.setStatus("PENDING");
        sampleDoc.setCreatedAt(Instant.now());
        sampleResponse = new OrderResponse("o-1", "c-1", "test order", "PENDING", sampleDoc.getCreatedAt());
    }
    @Test
    @DisplayName("getAllOrders con ORDER_VIEW restituisce la lista")
    void getAllOrders_customer_succeeds() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleDoc));
        when(orderMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        List<OrderResponse> result = orderService.getAllOrders(customer);
        assertThat(result).hasSize(1);
        verify(authorizationService).checkPermission(customer, Permission.ORDER_VIEW);
    }
    @Test
    @DisplayName("getAllOrders con FINANCE (ORDER_VIEW) non lancia eccezione")
    void getAllOrders_finance_viewAllowed() {
        when(orderRepository.findAll()).thenReturn(List.of(sampleDoc));
        when(orderMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        assertThatNoException().isThrownBy(() -> orderService.getAllOrders(finance));
    }
    @Test
    @DisplayName("getOrderById restituisce l'ordine esistente")
    void getOrderById_found() {
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(sampleDoc));
        when(orderMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        OrderResponse result = orderService.getOrderById(customer, "o-1");
        assertThat(result.getId()).isEqualTo("o-1");
    }
    @Test
    @DisplayName("getOrderById con id inesistente lancia OrderNotFoundException")
    void getOrderById_notFound_throws() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getOrderById(customer, "missing"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("missing");
    }
    @Test
    @DisplayName("createOrder con ORDER_CREATE salva l'ordine")
    void createOrder_customer_succeeds() {
        OrderRequest req = new OrderRequest();
        req.setUserId("c-1");
        req.setDescription("new order");
        when(orderMapper.toDocument(req)).thenReturn(sampleDoc);
        when(orderRepository.save(sampleDoc)).thenReturn(sampleDoc);
        when(orderMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        OrderResponse result = orderService.createOrder(customer, req);
        assertThat(result).isEqualTo(sampleResponse);
        verify(authorizationService).checkPermission(customer, Permission.ORDER_CREATE);
        verify(orderRepository).save(sampleDoc);
    }
    @Test
    @DisplayName("createOrder senza ORDER_CREATE lancia ForbiddenException")
    void createOrder_finance_forbidden() {
        doThrow(new ForbiddenException(Permission.ORDER_CREATE))
                .when(authorizationService).checkPermission(finance, Permission.ORDER_CREATE);
        OrderRequest req = new OrderRequest();
        req.setUserId("f-1");
        req.setDescription("order");
        assertThatThrownBy(() -> orderService.createOrder(finance, req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("ORDER_CREATE");
        verify(orderRepository, never()).save(any());
    }
    @Test
    @DisplayName("updateOrder con ORDER_CREATE aggiorna i campi")
    void updateOrder_customer_succeeds() {
        OrderRequest req = new OrderRequest();
        req.setUserId("c-1");
        req.setDescription("updated description");
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(sampleDoc));
        when(orderRepository.save(sampleDoc)).thenReturn(sampleDoc);
        when(orderMapper.toResponse(sampleDoc)).thenReturn(sampleResponse);
        orderService.updateOrder(customer, "o-1", req);
        verify(authorizationService).checkPermission(customer, Permission.ORDER_CREATE);
        verify(orderRepository).save(sampleDoc);
        assertThat(sampleDoc.getDescription()).isEqualTo("updated description");
    }
    @Test
    @DisplayName("updateOrder con id inesistente lancia OrderNotFoundException")
    void updateOrder_notFound_throws() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        OrderRequest req = new OrderRequest();
        req.setUserId("c-1");
        req.setDescription("desc");
        assertThatThrownBy(() -> orderService.updateOrder(customer, "missing", req))
                .isInstanceOf(OrderNotFoundException.class);
    }
    @Test
    @DisplayName("cancelOrder con ORDER_CANCEL imposta status CANCELLED")
    void cancelOrder_customer_succeeds() {
        when(orderRepository.findById("o-1")).thenReturn(Optional.of(sampleDoc));
        orderService.cancelOrder(customer, "o-1");
        verify(authorizationService).checkPermission(customer, Permission.ORDER_CANCEL);
        assertThat(sampleDoc.getStatus()).isEqualTo("CANCELLED");
        verify(orderRepository).save(sampleDoc);
    }
    @Test
    @DisplayName("cancelOrder senza ORDER_CANCEL lancia ForbiddenException")
    void cancelOrder_finance_forbidden() {
        doThrow(new ForbiddenException(Permission.ORDER_CANCEL))
                .when(authorizationService).checkPermission(finance, Permission.ORDER_CANCEL);
        assertThatThrownBy(() -> orderService.cancelOrder(finance, "o-1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("ORDER_CANCEL");
        verify(orderRepository, never()).save(any());
    }
    @Test
    @DisplayName("cancelOrder con id inesistente lancia OrderNotFoundException")
    void cancelOrder_notFound_throws() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.cancelOrder(customer, "missing"))
                .isInstanceOf(OrderNotFoundException.class);
    }
}