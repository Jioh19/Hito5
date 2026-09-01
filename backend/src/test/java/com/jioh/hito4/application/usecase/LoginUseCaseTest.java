package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.LoginUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private IIdentityRepository identityRepository;

    private LoginUseCase loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCase(identityRepository);
    }

    @Test
    void execute_returnsUser_whenCredentialsAreValid() {
        // Arrange
        User storedUser = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);
        when(identityRepository.Get("jioh")).thenReturn(storedUser);
        LoginUserRequest dto = new LoginUserRequest("jioh", "pass123");

        // Act
        UserResponse result = loginUseCase.execute(dto);

        // Assert
        assertEquals(storedUser.id(), result.id());
    }

    @Test
    void execute_normalizesUsernameCase_beforeLookup() {
        // Arrange
        User storedUser = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);
        when(identityRepository.Get("jioh")).thenReturn(storedUser);
        LoginUserRequest dto = new LoginUserRequest("JIOH", "pass123");

        // Act
        UserResponse result = loginUseCase.execute(dto);

        // Assert
        assertEquals(storedUser.id(), result.id());
    }

    @Test
    void execute_throwsUserNotFoundException_whenUsernameDoesNotExist() {
        // Arrange
        when(identityRepository.Get("ghost")).thenReturn(null);
        LoginUserRequest dto = new LoginUserRequest("ghost", "pass123");

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> loginUseCase.execute(dto));
    }

    @Test
    void execute_throwsUserNotFoundException_whenPasswordIsWrong() {
        // Arrange
        User storedUser = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);
        when(identityRepository.Get("jioh")).thenReturn(storedUser);
        LoginUserRequest dto = new LoginUserRequest("jioh", "wrongpass");

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> loginUseCase.execute(dto));
    }
}
