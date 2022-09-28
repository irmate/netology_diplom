package ru.netology.cloud_backend_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloud_backend_app.model.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Transactional
    @Override
    <S extends Token> S save(S entity);

    @Transactional(readOnly = true)
    @Query("select t from Token t where t.name = :name and t.status = 'ACTIVE'")
    Optional<Token> findByName(@Param("name") String name);

    @Transactional
    @Modifying
    @Query("update Token t set t.updated = CURRENT_TIMESTAMP, t.status = 'DELETED' where t.user.id = :userId")
    void updateStatus(@Param("userId") Long userId);
}