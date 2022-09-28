package ru.netology.cloud_backend_app.service.impl;

import org.springframework.stereotype.Service;
import ru.netology.cloud_backend_app.model.Token;
import ru.netology.cloud_backend_app.repository.TokenRepository;
import ru.netology.cloud_backend_app.service.BlacklistService;

@Service
public class BlacklistServiceImpl implements BlacklistService {
    private final TokenRepository tokenRepository;

    public BlacklistServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void addTokenToBlacklist(Token token) {
        tokenRepository.save(token);
    }

    @Override
    public boolean checkTokenAtBlacklist(String name) {
        return tokenRepository.findByName(name).isPresent();
    }

    @Override
    public void updateStatusOfToken(Long userId) {
        tokenRepository.updateStatus(userId);
    }
}