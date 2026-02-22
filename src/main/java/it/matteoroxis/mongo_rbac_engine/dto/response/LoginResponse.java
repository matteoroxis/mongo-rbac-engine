package it.matteoroxis.mongo_rbac_engine.dto.response;
public class LoginResponse {
    private final String token;
    private final String tokenType = "Bearer";
    public LoginResponse(String token) {
        this.token = token;
    }
    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
}