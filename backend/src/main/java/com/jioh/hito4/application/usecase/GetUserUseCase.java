package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import org.springframework.stereotype.Service;

@Service
public class GetUserUseCase {
    private final IIdentityRepository identityRepository;

    public GetUserUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public UserResponse execute(Integer id) {
        User user = identityRepository.GetById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username().value(), user.email().value());
    }
}
