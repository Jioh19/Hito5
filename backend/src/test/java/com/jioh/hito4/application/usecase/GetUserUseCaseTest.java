package com.jioh.hito4.application.usecase;

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
class GetUserUseCaseTest {

    @Mock
    private IIdentityRepository identityRepository;

    private GetUserUseCase getUserUseCase;

    @BeforeEach
    void setUp() {
        getUserUseCase = new GetUserUseCase(identityRepository);
    }

    @Test
    void execute_returnsUser_whenIdExists() {
        // Arrange
        User storedUser = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);
        when(identityRepository.GetById(1)).thenReturn(storedUser);

        // Act
        UserResponse result = getUserUseCase.execute(1);

        // Assert
        assertEquals(storedUser.id(), result.id());
        assertEquals(storedUser.username().value(), result.username());
        assertEquals(storedUser.email().value(), result.email());
    }

    @Test
    void execute_throwsUserNotFoundException_whenIdDoesNotExist() {
        // Arrange
        when(identityRepository.GetById(99)).thenReturn(null);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> getUserUseCase.execute(99));
    }
}
