package com.openjar.user_service.repository;

import com.openjar.user_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "SELECT * FROM users WHERE user_email = :email", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);
}
