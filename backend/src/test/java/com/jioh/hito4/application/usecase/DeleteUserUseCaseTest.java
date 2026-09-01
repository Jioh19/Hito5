package com.jioh.hito4.application.usecase;

import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private IIdentityRepository identityRepository;

    private DeleteUserUseCase deleteUserUseCase;

    @BeforeEach
    void setUp() {
        deleteUserUseCase = new DeleteUserUseCase(identityRepository);
    }

    @Test
    void execute_callsRepositoryDelete_whenUserExists() {
        // Arrange
        when(identityRepository.ExistsById(1)).thenReturn(true);

        // Act
        deleteUserUseCase.execute(1);

        // Assert
        verify(identityRepository, times(1)).Delete(1);
    }

    @Test
    void execute_throwsUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        when(identityRepository.ExistsById(99)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> deleteUserUseCase.execute(99));
        verify(identityRepository, never()).Delete(any());
    }
}
