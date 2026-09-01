package com.jioh.hito4.domain.repository;

import com.jioh.hito4.domain.entity.User;

import java.util.List;

public interface IIdentityRepository {
    User Get(String username);
    List<User> GetAll();
    User GetById(Integer id);
    boolean ExistsById(Integer id);
    boolean ExistsByEmail(String email);
    boolean ExistsByUsername(String username);
    User Create(User user);
    void Delete(Integer id);
}
