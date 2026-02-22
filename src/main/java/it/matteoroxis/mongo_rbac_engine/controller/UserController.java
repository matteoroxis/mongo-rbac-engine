package it.matteoroxis.mongo_rbac_engine.controller;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.dto.request.UserRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.UserResponse;
import it.matteoroxis.mongo_rbac_engine.resolver.UserPrincipalResolver;
import it.matteoroxis.mongo_rbac_engine.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserPrincipalResolver resolver;
    public UserController(UserService userService, UserPrincipalResolver resolver) {
        this.userService = userService;
        this.resolver = resolver;
    }
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        resolver.resolve(request);
        return ResponseEntity.ok(userService.getAllUsers());
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id, HttpServletRequest request) {
        resolver.resolve(request);
        return ResponseEntity.ok(userService.getUserById(id));
    }
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest body, HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(caller, body));
    }
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String id,
                                                   @Valid @RequestBody UserRequest body,
                                                   HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        return ResponseEntity.ok(userService.updateUser(caller, id, body));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id, HttpServletRequest request) {
        UserPrincipal caller = resolver.resolve(request);
        userService.deleteUser(caller, id);
        return ResponseEntity.noContent().build();
    }
}