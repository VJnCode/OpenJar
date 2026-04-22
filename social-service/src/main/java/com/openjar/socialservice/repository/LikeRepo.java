package com.openjar.socialservice.repository;


import com.openjar.socialservice.models.LikeInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface LikeRepo extends JpaRepository<LikeInteraction,Long> {

    @Query(value = "SELECT * FROM likes WHERE user_id = :userId AND recipe_id = :recipeId LIMIT 1",
            nativeQuery = true)
    Optional<LikeInteraction> findByUserIdAndRecipeId(@Param("userId") String userId,
                                                      @Param("recipeId") Long recipeId);

    @Query(value = "SELECT COUNT(*) FROM likes WHERE recipe_id = :recipeId",
            nativeQuery = true)
    long countByRecipeId(@Param("recipeId") Long recipeId);

//    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END FROM likes WHERE user_id = :userId AND recipe_id = :recipeId",
//            nativeQuery = true)
//    boolean existsByUserIdAndRecipeId(@Param("userId") Long userId,
//                                      @Param("recipeId") Long recipeId);


    @Query(value = "SELECT COUNT(*) FROM likes WHERE user_id = :userId AND recipe_id = :recipeId",
            nativeQuery = true)
    Long existsByUserIdAndRecipeId(@Param("userId") String userId,
                                   @Param("recipeId") Long recipeId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM likes WHERE user_id = :userId AND recipe_id = :recipeId",
            nativeQuery = true)
    void deleteByUserIdAndRecipeId(@Param("userId") String userId,
                                   @Param("recipeId") Long recipeId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO likes (user_id, recipe_id, created_at) VALUES (:userId, :recipeId, NOW())",
            nativeQuery = true)
    void insertLike(@Param("userId") String userId, @Param("recipeId") Long recipeId);
}


