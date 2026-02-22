package it.matteoroxis.mongo_rbac_engine.service;
import it.matteoroxis.mongo_rbac_engine.document.UserDocument;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.dto.request.UserRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.UserResponse;
import it.matteoroxis.mongo_rbac_engine.exception.UserNotFoundException;
import it.matteoroxis.mongo_rbac_engine.mapper.UserMapper;
import it.matteoroxis.mongo_rbac_engine.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;
    private final UserMapper userMapper;
    public UserService(UserRepository userRepository,
                       AuthorizationService authorizationService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
        this.userMapper = userMapper;
    }
    /** GET /users - any authenticated caller can read */
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }
    /** GET /users/{id} - any authenticated caller can read */
    public UserResponse getUserById(String id) {
        UserDocument doc = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponse(doc);
    }
    /** POST /users - requires USER_MANAGE */
    public UserResponse createUser(UserPrincipal caller, UserRequest request) {
        authorizationService.checkPermission(caller, Permission.USER_MANAGE);
        UserDocument doc = userMapper.toDocument(request);
        return userMapper.toResponse(userRepository.save(doc));
    }
    /** PUT /users/{id} - requires USER_MANAGE */
    public UserResponse updateUser(UserPrincipal caller, String id, UserRequest request) {
        authorizationService.checkPermission(caller, Permission.USER_MANAGE);
        UserDocument doc = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        doc.setEmail(request.getEmail());
        doc.setRoles(request.getRoles());
        doc.setStatus(request.getStatus());
        return userMapper.toResponse(userRepository.save(doc));
    }
    /** DELETE /users/{id} - requires USER_MANAGE */
    public void deleteUser(UserPrincipal caller, String id) {
        authorizationService.checkPermission(caller, Permission.USER_MANAGE);
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}