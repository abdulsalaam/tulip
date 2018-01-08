package tulip.app.exceptions;

public class RegistrationException extends RuntimeException {
    public RegistrationException() { super(); }
    public RegistrationException(String message) { super(message); }
}
