package ru.netology.cloud_backend_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloud_backend_app.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional(readOnly = true)
    User findByLogin(String login);
}