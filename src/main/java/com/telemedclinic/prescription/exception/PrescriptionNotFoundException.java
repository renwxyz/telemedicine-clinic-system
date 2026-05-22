package com.telemedclinic.prescription.exception;

public class PrescriptionNotFoundException extends Exception {
    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}