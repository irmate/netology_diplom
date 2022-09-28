package ru.netology.cloud_backend_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.cloud_backend_app.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}