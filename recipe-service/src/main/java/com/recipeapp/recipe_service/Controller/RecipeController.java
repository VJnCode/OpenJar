package com.recipeapp.recipe_service.Controller;


import com.recipeapp.recipe_service.DTO.RecipeRequestDto;
import com.recipeapp.recipe_service.Model.Recipe;
import com.recipeapp.recipe_service.Service.RecipeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipe")
public class RecipeController {

    private RecipeService service;
    public  RecipeController (RecipeService service){
        this.service=service;
    }

    @PostMapping
    public ResponseEntity<?> saveRecipe(@RequestBody RecipeRequestDto recipe,
                                        @RequestHeader("X-User-Id") Long userId){
//        recipe.setUserId(userId);
      String response=  service.saveRecipe(recipe, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Recipe added successfully");
    }

    @GetMapping
    public ResponseEntity<?>getAllRecipe(){
        List<Recipe> response=service.getAllRecipe();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{recipeId}")
    public ResponseEntity<?> getRecipe(@PathVariable long recipeId){
        Recipe response=service.getRecipeById( recipeId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{recipeId}")
    public  ResponseEntity<?>  deleteRecipe(@PathVariable long recipeId){
        String response=service.deleteRecipeById(recipeId);
        return ResponseEntity.ok(response);
    }


    @PutMapping("/{recipeId}")
    public ResponseEntity<?> updateRecipeById(@PathVariable long recipeId , @RequestBody  RecipeRequestDto updatedRecipe,
                                              @RequestHeader("X-User-Id") Long userId){
//        updatedRecipe.setUserId(userId);
        Recipe resposne =service.updateRecipeById(recipeId , updatedRecipe, userId);
        return ResponseEntity.ok(resposne);
    }





}
