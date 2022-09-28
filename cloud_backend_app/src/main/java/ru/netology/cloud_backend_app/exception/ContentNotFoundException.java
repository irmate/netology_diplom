package ru.netology.cloud_backend_app.exception;

public class ContentNotFoundException extends Exception {
    public ContentNotFoundException(String message) {
        super(message);
    }
}