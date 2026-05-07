package domain;

public class PrescriptionNotFoundException extends Exception {
    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}