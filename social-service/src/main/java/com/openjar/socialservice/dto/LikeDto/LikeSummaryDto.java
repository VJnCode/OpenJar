package com.openjar.socialservice.dto.LikeDto;

/** Like count for a recipe plus whether the current (optional) user has liked it. */
public record LikeSummaryDto(long totalLikes, boolean liked) {
}
