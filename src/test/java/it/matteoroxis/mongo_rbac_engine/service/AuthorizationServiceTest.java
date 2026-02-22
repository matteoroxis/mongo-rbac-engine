package it.matteoroxis.mongo_rbac_engine.service;
import it.matteoroxis.mongo_rbac_engine.domain.Permission;
import it.matteoroxis.mongo_rbac_engine.domain.UserPrincipal;
import it.matteoroxis.mongo_rbac_engine.domain.UserRole;
import it.matteoroxis.mongo_rbac_engine.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
class AuthorizationServiceTest {
    private AuthorizationService authorizationService;
    @BeforeEach
    void setUp() {
        authorizationService = new AuthorizationService();
    }
    @Test
    @DisplayName("ADMIN has all permissions")
    void admin_hasAllPermissions() {
        UserPrincipal admin = new UserPrincipal("admin-1", Set.of(UserRole.ADMIN));
        for (Permission p : Permission.values()) {
            assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(admin, p));
        }
    }
    @Test
    @DisplayName("CUSTOMER has ORDER_CREATE, ORDER_CANCEL, ORDER_VIEW")
    void customer_hasCorrectPermissions() {
        UserPrincipal customer = new UserPrincipal("c-1", Set.of(UserRole.CUSTOMER));
        assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(customer, Permission.ORDER_CREATE));
        assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(customer, Permission.ORDER_CANCEL));
        assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(customer, Permission.ORDER_VIEW));
    }
    @Test
    @DisplayName("CUSTOMER does not have REFUND_APPROVE -> ForbiddenException")
    void customer_missingRefundApprove_throws() {
        UserPrincipal customer = new UserPrincipal("c-1", Set.of(UserRole.CUSTOMER));
        assertThatThrownBy(() -> authorizationService.checkPermission(customer, Permission.REFUND_APPROVE))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("REFUND_APPROVE");
    }
    @Test
    @DisplayName("FINANCE has ORDER_VIEW and REFUND_APPROVE, but not ORDER_CREATE")
    void finance_hasCorrectPermissions() {
        UserPrincipal finance = new UserPrincipal("f-1", Set.of(UserRole.FINANCE));
        assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(finance, Permission.ORDER_VIEW));
        assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(finance, Permission.REFUND_APPROVE));
        assertThatThrownBy(() -> authorizationService.checkPermission(finance, Permission.ORDER_CREATE))
                .isInstanceOf(ForbiddenException.class);
    }
    @Test
    @DisplayName("User with multiple roles inherits all combined permissions")
    void multiRole_mergesPermissions() {
        UserPrincipal user = new UserPrincipal("u-1", Set.of(UserRole.CUSTOMER, UserRole.FINANCE));
        assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(user, Permission.ORDER_CREATE));
        assertThatNoException().isThrownBy(() -> authorizationService.checkPermission(user, Permission.REFUND_APPROVE));
    }
}