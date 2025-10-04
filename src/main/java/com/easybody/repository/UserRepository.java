package com.easybody.repository;

import com.easybody.model.entity.User;
import com.easybody.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCognitoSub(String cognitoSub);

    Optional<User> findByEmail(String email);

    boolean existsByCognitoSub(String cognitoSub);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndRole(Long id, Role role);
}

