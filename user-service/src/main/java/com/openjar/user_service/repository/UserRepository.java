package com.openjar.user_service.repository;

import com.openjar.user_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllUsersNative();

    @Query(value = "SELECT * FROM users WHERE user_id = :id", nativeQuery = true)
    Optional<User> findUserByIdNative(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO users (user_name, user_email, password, created_at, updated_at) " +
            "VALUES (:userName, :userEmail, :password, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)", nativeQuery = true)
    void insertUserNative(@Param("userName") String userName, @Param("userEmail") String userEmail, @Param("password") String password);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET user_name = :userName, user_email = :userEmail, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = :id", nativeQuery = true)
    void updateUserNative(@Param("id") Long id, @Param("userName") String userName, @Param("userEmail") String userEmail);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE user_id = :id", nativeQuery = true)
    void deleteUserNative(@Param("id") Long id);

    @Query(value = "SELECT COUNT(*) FROM users WHERE user_email = :email", nativeQuery = true)
    int countByUserEmailNative(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) FROM users WHERE user_name = :userName", nativeQuery = true)
    int countByUserNameNative(@Param("userName") String userName);
}