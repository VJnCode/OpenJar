package com.recipeapp.Like_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class LikeCountDto {
    private long totakLikes;
}
