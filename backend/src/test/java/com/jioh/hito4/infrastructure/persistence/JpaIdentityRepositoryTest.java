package com.jioh.hito4.infrastructure.persistence;

import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class JpaIdentityRepositoryTest {

    @Autowired
    private JpaIdentityRepository repository;

    @Test
    void create_assignsAnIdAndPersistsTheUser() {
        User toCreate = new User(null, new Username("jpauser"), "pass123", new Email("jpa@example.com"), Instant.now());

        User created = repository.Create(toCreate);

        assertNotNull(created.id());
        assertEquals("jpauser", created.username().value());
        assertTrue(repository.ExistsById(created.id()));
        assertTrue(repository.ExistsByUsername("jpauser"));
        assertTrue(repository.ExistsByEmail("jpa@example.com"));
    }

    @Test
    void get_returnsNull_whenUsernameDoesNotExist() {
        assertNull(repository.Get("nobody"));
    }

    @Test
    void delete_removesTheUser() {
        User created = repository.Create(new User(null, new Username("todelete"), "pass123", new Email("todelete@example.com"), Instant.now()));

        repository.Delete(created.id());

        assertFalse(repository.ExistsById(created.id()));
    }

    @Test
    void getAll_includesCreatedUsers() {
        repository.Create(new User(null, new Username("listuser"), "pass123", new Email("listuser@example.com"), Instant.now()));

        assertTrue(repository.GetAll().stream().anyMatch(u -> u.username().value().equals("listuser")));
    }
}
