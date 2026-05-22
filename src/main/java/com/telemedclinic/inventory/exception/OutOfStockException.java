package com.telemedclinic.inventory.exception;

public class OutOfStockException extends Exception {
    public OutOfStockException(String message) {
        super(message);
    }
}