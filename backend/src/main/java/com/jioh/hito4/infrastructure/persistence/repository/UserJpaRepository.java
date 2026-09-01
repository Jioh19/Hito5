package com.jioh.hito4.infrastructure.persistence.repository;

import com.jioh.hito4.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {
    // Spring Data JPA derives every query below from the method name — no manual SQL.
    Optional<UserEntity> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
