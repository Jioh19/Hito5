package com.jioh.hito4.application.usecase;

import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserUseCase {
    private final IIdentityRepository identityRepository;

    public DeleteUserUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public void execute(Integer id) {
        if (!identityRepository.ExistsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        identityRepository.Delete(id);
    }
}
