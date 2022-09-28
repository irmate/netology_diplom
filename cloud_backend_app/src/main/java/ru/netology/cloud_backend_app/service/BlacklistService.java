package ru.netology.cloud_backend_app.service;

import ru.netology.cloud_backend_app.model.Token;

public interface BlacklistService {
    void addTokenToBlacklist(Token token);
    boolean checkTokenAtBlacklist(String name);
    void updateStatusOfToken(Long userId);
}