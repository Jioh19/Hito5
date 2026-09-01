package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUsersUseCaseTest {

    @Mock
    private IIdentityRepository identityRepository;

    private GetUsersUseCase getUsersUseCase;

    @BeforeEach
    void setUp() {
        getUsersUseCase = new GetUsersUseCase(identityRepository);
    }

    @Test
    void execute_returnsEmptyList_whenNoUsersExist() {
        // Arrange
        when(identityRepository.GetAll()).thenReturn(List.of());

        // Act
        List<UserResponse> result = getUsersUseCase.execute();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void execute_returnsMappedUsers_excludingPassword() {
        // Arrange
        User storedUser = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);
        when(identityRepository.GetAll()).thenReturn(List.of(storedUser));

        // Act
        List<UserResponse> result = getUsersUseCase.execute();

        // Assert
        assertEquals(1, result.size());
        UserResponse response = result.get(0);
        assertEquals(storedUser.id(), response.id());
        assertEquals(storedUser.username().value(), response.username());
        assertEquals(storedUser.email().value(), response.email());
    }

    @Test
    void execute_preservesOrder_whenMultipleUsersExist() {
        // Arrange
        User first = new User(1, new Username("jioh"), "pass123", new Email("jioh@example.com"), Instant.EPOCH);
        User second = new User(2, new Username("naty"), "pass456", new Email("naty@example.com"), Instant.EPOCH);
        when(identityRepository.GetAll()).thenReturn(List.of(first, second));

        // Act
        List<UserResponse> result = getUsersUseCase.execute();

        // Assert
        assertEquals(List.of("jioh", "naty"), result.stream().map(UserResponse::username).toList());
    }
}
