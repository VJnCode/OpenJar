package com.openjar.recipe_service.Controller;

import com.openjar.recipe_service.DTO.RecipeRequestDto;
import com.openjar.recipe_service.Model.Recipe;
import com.openjar.recipe_service.Service.RecipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService service;

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return service.getAllRecipes();
    }

    @GetMapping("/user/{userId}")
    public List<Recipe> getRecipesByUser(@PathVariable String userId) {
        return service.getRecipesByUser(userId);
    }

    @GetMapping("/{recipeId}")
    public Recipe getRecipe(@PathVariable long recipeId) {
        return service.getRecipeById(recipeId);
    }

    @PostMapping
    public ResponseEntity<Recipe> createRecipe(@Valid @RequestBody RecipeRequestDto recipe,
                                               @AuthenticationPrincipal Jwt jwt) {
        Recipe saved = service.saveRecipe(recipe, CurrentUser.from(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{recipeId}")
    public Recipe updateRecipe(@PathVariable long recipeId,
                               @Valid @RequestBody RecipeRequestDto updatedRecipe,
                               @AuthenticationPrincipal Jwt jwt) {
        return service.updateRecipeById(recipeId, updatedRecipe, CurrentUser.from(jwt).userId());
    }

    @DeleteMapping("/{recipeId}")
    public Map<String, String> deleteRecipe(@PathVariable long recipeId, @AuthenticationPrincipal Jwt jwt) {
        service.deleteRecipeById(recipeId, CurrentUser.from(jwt).userId());
        return Map.of("message", "Recipe deleted");
    }

    /** Identity extracted from the access token issued by user-service. */
    public record CurrentUser(String userId, String userName, String email) {
        static CurrentUser from(Jwt jwt) {
            String uid = jwt == null ? null : jwt.getClaimAsString("uid");
            if (uid == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has no user id");
            }
            return new CurrentUser(uid, jwt.getClaimAsString("name"), jwt.getClaimAsString("email"));
        }
    }
}
