package com.recipeapp.recipe_service.Service;

import com.recipeapp.recipe_service.Event.LikeEvent;

import com.recipeapp.recipe_service.Repository.RecipeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeEventListener {


    private RecipeRepo recipeRepo;

    public RecipeEventListener (RecipeRepo recipeRepo){
        this.recipeRepo = recipeRepo;
    }

    @EventListener
    public void handleLikeToggled(LikeEvent event) {
        recipeRepo.updateLikeCount(event.getRecipeId(), event.getDelta());
    }
}
