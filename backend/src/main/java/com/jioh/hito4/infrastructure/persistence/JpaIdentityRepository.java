package com.jioh.hito4.infrastructure.persistence;

import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import com.jioh.hito4.infrastructure.persistence.entity.UserEntity;
import com.jioh.hito4.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaIdentityRepository implements IIdentityRepository {

    private final UserJpaRepository userJpaRepository;

    public JpaIdentityRepository(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User Get(String username) {
        return userJpaRepository.findByUsername(username).map(this::toDomain).orElse(null);
    }

    @Override
    public List<User> GetAll() {
        return userJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public User GetById(Integer id) {
        return userJpaRepository.findById(id).map(this::toDomain).orElse(null);
    }

    @Override
    public boolean ExistsById(Integer id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public boolean ExistsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean ExistsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public User Create(User user) {
        UserEntity saved = userJpaRepository.save(toEntity(user));
        return toDomain(saved);
    }

    @Override
    public void Delete(Integer id) {
        userJpaRepository.deleteById(id);
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(user.id(), user.username().value(), user.password(), user.email().value(), user.timestamp());
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), new Username(entity.getUsername()), entity.getPassword(), new Email(entity.getEmail()), entity.getCreatedAt());
    }
}
