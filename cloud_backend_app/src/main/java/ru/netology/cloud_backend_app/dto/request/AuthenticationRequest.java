package ru.netology.cloud_backend_app.dto.request;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String login;
    private String password;
}