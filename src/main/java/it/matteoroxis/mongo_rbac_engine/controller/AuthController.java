package it.matteoroxis.mongo_rbac_engine.controller;
import it.matteoroxis.mongo_rbac_engine.dto.request.LoginRequest;
import it.matteoroxis.mongo_rbac_engine.dto.response.LoginResponse;
import it.matteoroxis.mongo_rbac_engine.exception.UnauthorizedException;
import it.matteoroxis.mongo_rbac_engine.repository.UserRepository;
import it.matteoroxis.mongo_rbac_engine.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }
    /**
     * POST /auth/login
     * Body: { "userId": "<mongo_id>" }
     * Returns a signed JWT valid for 24 h.
     *
     * In a real system credentials (e.g. password hash) would be verified here.
     * This demo implementation issues a token based on userId only.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest body) {
        userRepository.findById(body.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found: " + body.getUserId()));
        String token = jwtService.generateToken(body.getUserId());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}