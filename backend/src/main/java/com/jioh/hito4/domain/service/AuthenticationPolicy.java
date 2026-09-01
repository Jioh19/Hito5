package com.jioh.hito4.domain.service;

import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserNotFoundException;

public class AuthenticationPolicy {

    public void authenticate(User user, String rawPassword) {
        if (user == null) {
            throw new UserNotFoundException("Wrong credentials");
        }
        if (!user.password().equals(rawPassword)) {
            throw new UserNotFoundException("Wrong credentials");
        }
    }
}
