package it.matteoroxis.mongo_rbac_engine.exception;
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) {
        super("Order not found: " + id);
    }
}