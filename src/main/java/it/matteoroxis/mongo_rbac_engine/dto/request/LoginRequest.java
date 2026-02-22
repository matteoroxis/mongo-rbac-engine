package it.matteoroxis.mongo_rbac_engine.dto.request;
import jakarta.validation.constraints.NotBlank;
public class LoginRequest {
    @NotBlank(message = "userId is required")
    private String userId;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}