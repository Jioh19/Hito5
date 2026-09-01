package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RegisterUserUseCase {
    private final IIdentityRepository identityRepository;

    public RegisterUserUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public UserResponse execute(RegisterUserRequest registerUser) {
        Username username = new Username(registerUser.username());
        Email email = new Email(registerUser.email());

        if (identityRepository.ExistsByEmail(email.value())) {
            throw new UserAlreadyExistsException("User already exists with email: " + registerUser.email());
        }
        if (identityRepository.ExistsByUsername(username.value())) {
            throw new UserAlreadyExistsException("User already exists with username: " + registerUser.username());
        }

        User created = identityRepository.Create(new User(null, username, registerUser.password(), email, Instant.now()));
        return toResponse(created);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username().value(), user.email().value());
    }
}
