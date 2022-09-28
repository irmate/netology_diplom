package ru.netology.cloud_backend_app.service;

import ru.netology.cloud_backend_app.model.User;

public interface UserService {
    User findByLogin(String login);
}