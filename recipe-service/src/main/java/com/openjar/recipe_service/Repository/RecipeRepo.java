package com.openjar.recipe_service.Repository;

import com.openjar.recipe_service.Model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByOrderByRecipeIdDesc();

    List<Recipe> findByUserIdOrderByRecipeIdDesc(String userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE recipe SET like_count = GREATEST(like_count + :delta, 0) WHERE recipe_id = :recipeId",
            nativeQuery = true)
    void updateLikeCount(@Param("recipeId") Long recipeId, @Param("delta") int delta);
}
