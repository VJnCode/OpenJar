package com.recipeapp.Like_service.Service;


import com.recipeapp.Like_service.Cofigurations.RabbitMQConfig;
import com.recipeapp.Like_service.DTO.LikeDto;
import com.recipeapp.Like_service.Event.LikeEvent;
import com.recipeapp.Like_service.Repository.LikeRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
public class LikeService {

    private LikeRepo likeRepository;
    private final RabbitTemplate rabbitTemplate;
//    private final ApplicationEventPublisher eventPublisher;

    public LikeService(LikeRepo repo, RabbitTemplate rabbitTemplate) {
        this.likeRepository = repo;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public LikeDto toggleLike(Long userId, Long recipeId) {
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
            long totalLikes = likeRepository.countByRecipeId(recipeId);
            return LikeDto.of("LIKED", totalLikes);
        }
    }

    public long getLikeCount(Long recipeId) {
        return likeRepository.countByRecipeId(recipeId);
    }

    public Long hasUserLiked(Long userId, Long recipeId) {
        return likeRepository.existsByUserIdAndRecipeId(userId, recipeId);
    }
}
