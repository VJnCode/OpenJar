package com.openjar.user_service.repository;

import com.openjar.user_service.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "SELECT * FROM users",
            countQuery = "SELECT COUNT(*) FROM users",
            nativeQuery = true)
    Page<User> findAllUsersNative(Pageable pageable);

    @Query(value = "SELECT * FROM users WHERE user_id = :id", nativeQuery = true)
    Optional<User> findUserByIdNative(@Param("id") String id);

    // 1. Updated Insert Query
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users (user_id, user_name, user_email, password, created_at, is_verified, otp) " +
            "VALUES (:userId, :userName, :userEmail, :password, NOW(), false, :otp)", nativeQuery = true)
    void insertUserNative(
            @Param("userId") String userId,
            @Param("userName") String userName,
            @Param("userEmail") String userEmail,
            @Param("password") String password,
            @Param("otp") String otp  // <--- Added OTP parameter
    );

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_verified = true, otp = NULL WHERE user_email = :email AND otp = :otp", nativeQuery = true)
    int verifyUserNative(@Param("email") String email, @Param("otp") String otp);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET user_name = :userName, user_email = :userEmail " +
            "WHERE user_id = :id", nativeQuery = true)
    void updateUserNative(@Param("id") String id, @Param("userName") String userName, @Param("userEmail") String userEmail);

    // Add this missing method to check for duplicate emails
    @Query(value = "SELECT COUNT(*) FROM users WHERE user_email = :email", nativeQuery = true)
    int checkEmailExistsNative(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE user_id = :id", nativeQuery = true)
    void deleteUserNative(@Param("id") String id);

    @Query(value = "SELECT COUNT(*) FROM users WHERE user_email = :email", nativeQuery = true)
    int countByUserEmailNative(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) FROM users WHERE user_name = :userName", nativeQuery = true)
    int countByUserNameNative(@Param("userName") String userName);
}