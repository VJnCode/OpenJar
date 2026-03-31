package com.recipeapp.recipe_service.Service;

import com.recipeapp.recipe_service.Configuration.RabbitMQConfig;
import com.recipeapp.recipe_service.Event.LikeEvent;

import com.recipeapp.recipe_service.Repository.RecipeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class RecipeEventListener {

    private final RecipeRepo recipeRepo;

    @RabbitListener(queues = RabbitMQConfig.LIKE_QUEUE) // ← change this
    public void handleLikeToggled(LikeEvent event) {
        recipeRepo.updateLikeCount(event.getRecipeId(), event.getDelta());
    }
}
