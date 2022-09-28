package ru.netology.cloud_backend_app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloud_backend_app.model.Content;

import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {

    @Transactional
    @Override
    <S extends Content> S saveAndFlush(S entity);

    @Transactional
    @Modifying
    @Query("update Content c set c.updated = CURRENT_TIMESTAMP, c.status = 'DELETED' " +
            "where c.name = :name and c.user.id = :userId")
    void delete(@Param("name") String name, @Param("userId") Long userId);

    @Transactional(readOnly = true)
    @Query("select c from Content c " +
            "where c.status = 'ACTIVE' and c.name = :name and c.user.id = :userId")
    Optional<Content> findByName(@Param("name") String name, @Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("update Content c set c.updated = CURRENT_TIMESTAMP, c.name = :name " +
            "where c.status = 'ACTIVE' and c.name = :filename and c.user.id = :userId")
    void update(@Param("name") String name, @Param("filename") String filename, @Param("userId") Long userId);

    @Transactional(readOnly = true)
    @Query("select c from Content c " +
            "where c.status = 'ACTIVE' and c.user.id = :userId")
    Page<Content> findAll(Pageable pageable, @Param("userId") Long userId);
}