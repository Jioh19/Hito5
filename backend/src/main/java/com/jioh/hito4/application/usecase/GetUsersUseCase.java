package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetUsersUseCase {
    private final IIdentityRepository identityRepository;

    public GetUsersUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public List<UserResponse> execute() {
        List<User> users = identityRepository.GetAll();
        return users.stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username().value(), user.email().value());
    }
}
