package it.matteoroxis.mongo_rbac_engine.controller;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.dto.request.OrderRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.OrderResponse;
import it.matteoroxis.mongo_rbac_engine.resolver.UserPrincipalResolver;
import it.matteoroxis.mongo_rbac_engine.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserPrincipalResolver resolver;
    public OrderController(OrderService orderService, UserPrincipalResolver resolver) {
        this.orderService = orderService;
        this.resolver = resolver;
    }
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        return ResponseEntity.ok(orderService.getAllOrders(caller));
    }
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id, HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        return ResponseEntity.ok(orderService.getOrderById(caller, id));
    }
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest body, HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(caller, body));
    }
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable String id,
                                                     @Valid @RequestBody OrderRequest body,
                                                     HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        return ResponseEntity.ok(orderService.updateOrder(caller, id, body));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String id, HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        orderService.cancelOrder(caller, id);
        return ResponseEntity.noContent().build();
    }
}