package com.openjar.socialservice.dto.LikeDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "of")
public class LikeDto {
    private String status;
    private long totalLikes;

}
