package it.matteoroxis.mongo_rbac_engine.dto.request;
import jakarta.validation.constraints.NotBlank;
public class OrderRequest {
    @NotBlank(message = "UserId is required")
    private String userId;
    @NotBlank(message = "Description is required")
    private String description;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}