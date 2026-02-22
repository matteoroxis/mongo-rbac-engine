package it.matteoroxis.mongo_rbac_engine.exception;
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) {
        super("User not found: " + id);
    }
}