package com.recipeapp.Like_service.Controller;


import com.recipeapp.Like_service.DTO.LikeCountDto;
import com.recipeapp.Like_service.DTO.LikeDto;
import com.recipeapp.Like_service.DTO.LikeStatusDto;
import com.recipeapp.Like_service.Service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/like")
public class LikeController {

    private LikeService likeService;
    public LikeController(LikeService likeService){
        this.likeService=likeService;
    }

    @PostMapping("/{recipeId}/{userId}")
    public ResponseEntity<LikeDto> toggleLike(
            @PathVariable Long recipeId,
          @PathVariable String userId) {
        LikeDto response = likeService.toggleLike(userId, recipeId);
        return ResponseEntity.ok(response);
    }

    // Get total like count for a recipe
    @GetMapping("/{recipeId}")
    public ResponseEntity<LikeCountDto> getLikeCount(
            @PathVariable Long recipeId) {
        long count = likeService.getLikeCount(recipeId);
        return ResponseEntity.ok(LikeCountDto.of( count));
    }

    // Check if a specific user has liked a recipe
    @GetMapping("/{recipeId}/{userId}/status")
    public ResponseEntity<LikeStatusDto> getLikeStatus(
            @PathVariable Long recipeId,
           @PathVariable String userId) {
        Long liked = likeService.hasUserLiked(userId, recipeId);
        if(liked!=0){
            return ResponseEntity.ok(LikeStatusDto.of(true));
        }
        return ResponseEntity.ok(LikeStatusDto.of( false));
    }






}
