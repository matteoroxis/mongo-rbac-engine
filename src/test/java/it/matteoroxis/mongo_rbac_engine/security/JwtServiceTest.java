package it.matteoroxis.mongo_rbac_engine.security;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class JwtServiceTest {
    private static final String SECRET = "4d6f6e676f52626163456e67696e655365637265744b657932303236212121";
    private static final long EXPIRATION_MS = 86400000L;
    private JwtService jwtService;
    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }
    @Test
    @DisplayName("generateToken returns a non-null token")
    void generateToken_returnsNonNull() {
        String token = jwtService.generateToken("user-123");
        assertThat(token).isNotBlank();
    }
    @Test
    @DisplayName("extractUserId returns the correct subject")
    void extractUserId_returnsCorrectSubject() {
        String token = jwtService.generateToken("user-abc");
        String extracted = jwtService.extractUserId(token);
        assertThat(extracted).isEqualTo("user-abc");
    }
    @Test
    @DisplayName("extractUserId throws an exception for a tampered token")
    void extractUserId_throwsOnTamperedToken() {
        String token = jwtService.generateToken("user-123") + "tampered";
        assertThatThrownBy(() -> jwtService.extractUserId(token))
                .isInstanceOf(Exception.class);
    }
    @Test
    @DisplayName("expired token throws an exception")
    void extractUserId_throwsOnExpiredToken() throws InterruptedException {
        JwtService shortLived = new JwtService(SECRET, 1L); // 1 ms
        String token = shortLived.generateToken("user-xyz");
        Thread.sleep(10);
        assertThatThrownBy(() -> shortLived.extractUserId(token))
                .isInstanceOf(Exception.class);
    }
}