package com.jioh.hito4.domain.entity;

import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;

import java.time.Instant;
import java.util.Objects;

public class User {
    private final Integer id;
    private final Username username;
    private final String password;
    private final Email email;
    private final Instant timestamp;

    public User(Integer id, Username username, String password, Email email, Instant timestamp) {
        if (username == null) throw new IllegalArgumentException("Username must not be null");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password must not be empty");
        if (email == null) throw new IllegalArgumentException("Email must not be null");
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.timestamp = timestamp;
    }

    public Integer id() {
        return id;
    }

    public Username username() {
        return username;
    }

    public String password() {
        return password;
    }

    public Email email() {
        return email;
    }

    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
