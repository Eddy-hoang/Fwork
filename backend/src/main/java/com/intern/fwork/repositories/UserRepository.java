package com.intern.fwork.repositories;

import com.intern.fwork.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    //SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String mail);

    //dùng cho đăng kí
    //SELECT EXISTS (
    //    SELECT *
    //    FROM users
    //    WHERE email = ?
    //);
    boolean existsByEmail(String email);

    java.util.List<User> findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(String email, String name);
}
