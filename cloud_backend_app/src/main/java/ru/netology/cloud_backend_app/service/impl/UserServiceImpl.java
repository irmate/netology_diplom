package ru.netology.cloud_backend_app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.netology.cloud_backend_app.model.User;
import ru.netology.cloud_backend_app.repository.RoleRepository;
import ru.netology.cloud_backend_app.repository.UserRepository;
import ru.netology.cloud_backend_app.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}