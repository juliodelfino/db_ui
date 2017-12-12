package com.delfino.util;

public class AppException extends Exception {
    public AppException(String message) {
        super(message);
    }

    public AppException(Exception ex) {
        super(ex);
    }

    public AppException(String message, Exception ex) {
        super(message, ex);
    }
}
