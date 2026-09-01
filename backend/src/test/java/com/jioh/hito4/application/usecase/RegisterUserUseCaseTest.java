package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.InvalidEmailException;
import com.jioh.hito4.domain.exception.InvalidUsernameException;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private IIdentityRepository identityRepository;

    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(identityRepository);
    }

    @Test
    void execute_callsRepositoryCreate_whenNoDuplicates() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(false);
        when(identityRepository.ExistsByUsername("jioh")).thenReturn(false);
        when(identityRepository.Create(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new User(1, u.username(), u.password(), u.email(), u.timestamp());
        });

        UserResponse result = registerUserUseCase.execute(dto);

        verify(identityRepository, times(1)).Create(any(User.class));
        assertEquals(1, result.id());
        assertEquals("jioh", result.username());
        assertEquals("jioh@example.com", result.email());
    }

    @Test
    void execute_throwsUserAlreadyExistsException_whenUsernameIsDuplicatedWithDifferentCase() {
        RegisterUserRequest dto = new RegisterUserRequest("JIOH", "pass123", "new@example.com");
        when(identityRepository.ExistsByEmail("new@example.com")).thenReturn(false);
        when(identityRepository.ExistsByUsername("jioh")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.execute(dto));
        verify(identityRepository, never()).Create(any(User.class));
    }

    @Test
    void execute_passesUnpersistedUserWithNullId_toRepository() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(false);
        when(identityRepository.ExistsByUsername("jioh")).thenReturn(false);
        when(identityRepository.Create(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new User(1, u.username(), u.password(), u.email(), u.timestamp());
        });

        registerUserUseCase.execute(dto);

        verify(identityRepository).Create(org.mockito.ArgumentMatchers.argThat(user ->
                user.id() == null &&
                user.username().value().equals("jioh") &&
                user.password().equals("pass123") &&
                user.email().value().equals("jioh@example.com")
        ));
    }

    @Test
    void execute_throwsUserAlreadyExistsException_whenEmailIsDuplicated() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.execute(dto));
        verify(identityRepository, never()).Create(any(User.class));
    }

    @Test
    void execute_throwsUserAlreadyExistsException_whenUsernameIsDuplicated() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(false);
        when(identityRepository.ExistsByUsername("jioh")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.execute(dto));
        verify(identityRepository, never()).Create(any(User.class));
    }

    @Test
    void execute_throwsInvalidUsernameException_whenUsernameIsBlank() {
        RegisterUserRequest dto = new RegisterUserRequest("", "pass123", "jioh@example.com");

        assertThrows(InvalidUsernameException.class, () -> registerUserUseCase.execute(dto));
    }

    @Test
    void execute_throwsIllegalArgumentException_whenPasswordIsBlank() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "", "jioh@example.com");
        when(identityRepository.ExistsByEmail(anyString())).thenReturn(false);
        when(identityRepository.ExistsByUsername(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> registerUserUseCase.execute(dto));
    }

    @Test
    void execute_throwsInvalidEmailException_whenEmailIsBlank() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "");

        assertThrows(InvalidEmailException.class, () -> registerUserUseCase.execute(dto));
    }
}
