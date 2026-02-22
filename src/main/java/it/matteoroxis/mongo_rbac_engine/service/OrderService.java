package it.matteoroxis.mongo_rbac_engine.service;
import it.matteoroxis.mongo_rbac_engine.document.OrderDocument;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.dto.request.OrderRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.OrderResponse;
import it.matteoroxis.mongo_rbac_engine.exception.OrderNotFoundException;
import it.matteoroxis.mongo_rbac_engine.mapper.OrderMapper;
import it.matteoroxis.mongo_rbac_engine.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final AuthorizationService authorizationService;
    private final OrderMapper orderMapper;
    public OrderService(OrderRepository orderRepository,
                        AuthorizationService authorizationService,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.authorizationService = authorizationService;
        this.orderMapper = orderMapper;
    }
    /** GET /orders - requires ORDER_VIEW */
    public List<OrderResponse> getAllOrders(UserPrincipal caller) {
        authorizationService.checkPermission(caller, Permission.ORDER_VIEW);
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
    /** GET /orders/{id} - requires ORDER_VIEW */
    public OrderResponse getOrderById(UserPrincipal caller, String id) {
        authorizationService.checkPermission(caller, Permission.ORDER_VIEW);
        OrderDocument doc = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return orderMapper.toResponse(doc);
    }
    /** POST /orders - requires ORDER_CREATE */
    public OrderResponse createOrder(UserPrincipal caller, OrderRequest request) {
        authorizationService.checkPermission(caller, Permission.ORDER_CREATE);
        OrderDocument doc = orderMapper.toDocument(request);
        return orderMapper.toResponse(orderRepository.save(doc));
    }
    /** PUT /orders/{id} - requires ORDER_CREATE */
    public OrderResponse updateOrder(UserPrincipal caller, String id, OrderRequest request) {
        authorizationService.checkPermission(caller, Permission.ORDER_CREATE);
        OrderDocument doc = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        doc.setDescription(request.getDescription());
        doc.setUserId(request.getUserId());
        return orderMapper.toResponse(orderRepository.save(doc));
    }
    /** DELETE /orders/{id} - requires ORDER_CANCEL */
    public void cancelOrder(UserPrincipal caller, String id) {
        authorizationService.checkPermission(caller, Permission.ORDER_CANCEL);
        OrderDocument doc = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        doc.setStatus("CANCELLED");
        orderRepository.save(doc);
    }
}