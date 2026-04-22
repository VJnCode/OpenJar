package com.recipeapp.Like_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeNotificationDto {
    private String ownerName;
    private String receiverName;
    private String receiverEmail;
    private String recipeName;

}
