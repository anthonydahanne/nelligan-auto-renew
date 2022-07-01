package net.dahanne.nelligan.auto.renew;

public class UnRenewableItemException extends RuntimeException {
    public UnRenewableItemException(String message) {
        super(message);
    }
}
