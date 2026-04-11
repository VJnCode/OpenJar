package com.recipeapp.Like_service.Service;

//
//import com.recipeapp.Like_service.Cofigurations.RabbitMQConfig;
//import com.recipeapp.Like_service.Cofigurations.WebClientConfig;
//import com.recipeapp.Like_service.DTO.LikeDto;
//import com.recipeapp.Like_service.DTO.LikeNotificationDto;
//import com.recipeapp.Like_service.DTO.RecipeDTO;
//import com.recipeapp.Like_service.DTO.UserDto;
//import com.recipeapp.Like_service.Event.LikeEvent;
//import com.recipeapp.Like_service.Repository.LikeRepo;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.web.reactive.function.client.WebClient;
//@Slf4j
//@Service
//public class LikeService {
//
//    private LikeRepo likeRepository;
//    private final RabbitTemplate rabbitTemplate;
//
//
//
////    private final WebClient.Builder webClientBuilder;
//private final WebClient webClientBuilder;
//
//    public LikeService(LikeRepo repo, RabbitTemplate rabbitTemplate, WebClient.Builder webClientBuilder) {
//        this.likeRepository = repo;
//        this.rabbitTemplate = rabbitTemplate;
////        this.webClientBuilder = webClientBuilder;
//        this.webClientBuilder = webClientBuilder.baseUrl("http://localhost:8086").build();
//
//    }
//
//
//    @Transactional
//    public LikeDto toggleLike(String userId, Long recipeId) {
//        Long alreadyLiked = likeRepository.existsByUserIdAndRecipeId(userId, recipeId);
//
//        if (alreadyLiked!=0) {
//            likeRepository.deleteByUserIdAndRecipeId(userId, recipeId);
//            rabbitTemplate.convertAndSend(
//                    RabbitMQConfig.EXCHANGE,
//                    RabbitMQConfig.LIKE_ROUTING_KEY,
//                    new LikeEvent(recipeId, -1)  // ← change this
//            );
//            long totalLikes = likeRepository.countByRecipeId(recipeId);
//            return LikeDto.of("UNLIKED", totalLikes);
//        } else {
//            likeRepository.insertLike(userId, recipeId);
//            rabbitTemplate.convertAndSend(
//                    RabbitMQConfig.EXCHANGE,
//                    RabbitMQConfig.LIKE_ROUTING_KEY,
//                    new LikeEvent(recipeId, +1)  // ← change this
//            );
//            try {
//                RecipeDTO recipe =webClientBuilder.get()
//                        .uri("http://recipe-service/api/recipes/{id}", recipeId)
//                        .retrieve()
//                        .bodyToMono(RecipeDTO.class)
//                        .block();
//
//                UserDto recipeOwner = webClientBuilder.get()
//                        .uri("http://user-service/api/users/{id}", recipe.getUserId())
//                        .retrieve()
//                        .bodyToMono(UserDto.class)
//                        .block();
//
//                UserDto likedByUser = webClientBuilder.get()
//                        .uri("http://user-service/api/users/{id}", userId)
//                        .retrieve()
//                        .bodyToMono(UserDto.class)
//                        .block();
//
//                rabbitTemplate.convertAndSend(
//                        RabbitMQConfig.EXCHANGE,
//                        RabbitMQConfig.LIKE_Recipe_ROUTING_KEY ,
//                        new LikeNotificationDto(
//                                likedByUser.getUserName(),
//                                recipeOwner.getUserName(),
//                                recipeOwner.getUserEmail(),
//                                recipe.getRecipeName()
//                        )
//                );
//                log.info("Like notification sent for recipe {}", recipeId);
//
//            } catch (Exception e) {
//                // notification failed but like still works!
//                log.error(" Failed to send like notification: {}", e.getMessage());
//            }
//
//            long totalLikes = likeRepository.countByRecipeId(recipeId);
//            return LikeDto.of("LIKED", totalLikes);
//        }
//    }
//
//    public long getLikeCount(Long recipeId) {
//        return likeRepository.countByRecipeId(recipeId);
//    }
//
//    public Long hasUserLiked(String userId, Long recipeId) {
//        return likeRepository.existsByUserIdAndRecipeId(userId, recipeId);
//    }
//}

import com.recipeapp.Like_service.Cofigurations.RabbitMQConfig;
import com.recipeapp.Like_service.Cofigurations.WebClientConfig;
import com.recipeapp.Like_service.DTO.LikeDto;
import com.recipeapp.Like_service.DTO.LikeNotificationDto;
import com.recipeapp.Like_service.DTO.RecipeDTO;
import com.recipeapp.Like_service.DTO.UserDto;
import com.recipeapp.Like_service.Event.LikeEvent;
import com.recipeapp.Like_service.Repository.LikeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.reactive.function.client.WebClient;
@Slf4j
@Service
public class LikeService {

    private LikeRepo likeRepository;
    private final RabbitTemplate rabbitTemplate;

    // ✅ CHANGE 1: Back to WebClient.Builder
    private final WebClient.Builder webClientBuilder;

    public LikeService(LikeRepo repo, RabbitTemplate rabbitTemplate, WebClient.Builder webClientBuilder) {
        this.likeRepository = repo;
        this.rabbitTemplate = rabbitTemplate;
        // ✅ CHANGE 2: Just store the builder, no baseUrl or build()
        this.webClientBuilder = webClientBuilder;
    }


    @Transactional
    public LikeDto toggleLike(String userId, Long recipeId) {
        Long alreadyLiked = likeRepository.existsByUserIdAndRecipeId(userId, recipeId);

        if (alreadyLiked!=0) {
            likeRepository.deleteByUserIdAndRecipeId(userId, recipeId);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.LIKE_ROUTING_KEY,
                    new LikeEvent(recipeId, -1)  // ← change this
            );
            long totalLikes = likeRepository.countByRecipeId(recipeId);
            return LikeDto.of("UNLIKED", totalLikes);
        } else {
            likeRepository.insertLike(userId, recipeId);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.LIKE_ROUTING_KEY,
                    new LikeEvent(recipeId, +1)  // ← change this
            );
            try {
                RecipeDTO recipe = webClientBuilder.build()
                        .get()
                        .uri("http://recipe-service/recipe/{id}", recipeId)
                        .retrieve()
                        .bodyToMono(RecipeDTO.class)
                        .block();
                log.info("Recipe fetched: {}", recipe);
                UserDto recipeOwner = webClientBuilder.build()
                        .get()
                        .uri("http://user-service/api/users/{id}", recipe.getUserId())
                        .retrieve()
                        .bodyToMono(UserDto.class)
                        .block();
                log.info("Ownner fetched: {}", recipeOwner);

                UserDto likedByUser = webClientBuilder.build()
                        .get()
                        .uri("http://user-service/api/users/{id}", userId)
                        .retrieve()
                        .bodyToMono(UserDto.class)
                        .block();
                log.info("LikedByUser fetched: {}", likedByUser);

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.LIKE_Recipe_ROUTING_KEY ,
                        new LikeNotificationDto(
                                recipeOwner.getUserName(),   // ownerName   → recipe owner
                                likedByUser.getUserName(),   // receiverName → who liked
                                recipeOwner.getUserEmail(),  // receiverEmail → owner's email
                                recipe.getRecipeName()       // recipeName
                        )
                );
                log.info("Like notification sent for recipe {}", recipeId);

            } catch (Exception e) {
                // notification failed but like still works!
                log.error(" Failed to send like notification: {}", e.getMessage());
            }

            long totalLikes = likeRepository.countByRecipeId(recipeId);
            return LikeDto.of("LIKED", totalLikes);
        }
    }

    public long getLikeCount(Long recipeId) {
        return likeRepository.countByRecipeId(recipeId);
    }

    public Long hasUserLiked(String userId, Long recipeId) {
        return likeRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }
}
