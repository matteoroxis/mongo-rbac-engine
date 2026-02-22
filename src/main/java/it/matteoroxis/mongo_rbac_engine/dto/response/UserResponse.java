package it.matteoroxis.mongo_rbac_engine.dto.response;
import java.util.Set;
public class UserResponse {
    private String id;
    private String email;
    private Set<String> roles;
    private String status;
    public UserResponse(String id, String email, Set<String> roles, String status) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.status = status;
    }
    public String getId() { return id; }
    public String getEmail() { return email; }
    public Set<String> getRoles() { return roles; }
    public String getStatus() { return status; }
}