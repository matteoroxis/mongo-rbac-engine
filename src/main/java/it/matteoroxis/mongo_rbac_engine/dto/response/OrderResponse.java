package it.matteoroxis.mongo_rbac_engine.dto.response;
import java.time.Instant;
public class OrderResponse {
    private String id;
    private String userId;
    private String description;
    private String status;
    private Instant createdAt;
    public OrderResponse(String id, String userId, String description, String status, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}