package com.recipeapp.recipe_service.Repository;

import com.recipeapp.recipe_service.Model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface RecipeRepo extends JpaRepository<Recipe,Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO recipe (recipe_name, prep_time, category, ingredients, recipe_instructions, recipe_image_url, user_id) " +
            "VALUES (:name, :prepTime, :category, :ingredients, :instructions, :imageUrl, :userId)", nativeQuery = true)
    int insertRecipe(
            @Param("name") String name,
            @Param("prepTime") int prepTime,
            @Param("category") String category,
            @Param("ingredients") String ingredients,
            @Param("instructions") String instructions,
            @Param("imageUrl") String imageUrl,
            @Param("userId") Long userId
    );

    // ✅ GET ALL
    @Query(value = "SELECT * FROM recipe", nativeQuery = true)
    List<Recipe> getAllRecipe();

    // ✅ GET BY ID
    @Query(value = "SELECT * FROM recipe WHERE recipe_id = :id", nativeQuery = true)
    Recipe getRecipeById(@Param("id") Long id);

    // ✅ DELETE
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM recipe WHERE recipe_id = :id", nativeQuery = true)
    int deleteRecipeById(@Param("id") Long id);

    // ✅ UPDATE
    @Modifying
    @Transactional
    @Query(value = "UPDATE recipe SET recipe_name = :name, prep_time = :prepTime, category = :category, " +
            "ingredients = :ingredients, recipe_instructions = :instructions, recipe_image_url = :imageUrl, user_id = :userId " +
            "WHERE recipe_id = :id", nativeQuery = true)
    int updateRecipe(
            @Param("name") String name,
            @Param("prepTime") int prepTime,
            @Param("category") String category,
            @Param("ingredients") String ingredients,
            @Param("instructions") String instructions,
            @Param("imageUrl") String imageUrl,
            @Param("userId") Long userId,
            @Param("id") Long id
    );





}
