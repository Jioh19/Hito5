package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.LoginUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.service.AuthenticationPolicy;
import com.jioh.hito4.domain.valueobject.Username;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {
    private final IIdentityRepository identityRepository;
    private final AuthenticationPolicy authenticationPolicy = new AuthenticationPolicy();

    public LoginUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public UserResponse execute(LoginUserRequest loginUserRequest) {
        User user = identityRepository.Get(new Username(loginUserRequest.username()).value());
        authenticationPolicy.authenticate(user, loginUserRequest.password());
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username().value(), user.email().value());
    }
}
