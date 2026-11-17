package com.openjar.socialservice.Controller;

import com.openjar.socialservice.config.CurrentUser;
import com.openjar.socialservice.dto.LikeDto.LikeSummaryDto;
import com.openjar.socialservice.sevice.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Lives under /api/social so the gateway can route it (the old /like path was unreachable). */
@RestController
@RequestMapping("/api/social/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /** Toggle the current user's like on a recipe. */
    @PostMapping("/{recipeId}")
    public LikeSummaryDto toggleLike(@PathVariable Long recipeId, @AuthenticationPrincipal Jwt jwt) {
        return likeService.toggleLike(recipeId, CurrentUser.from(jwt));
    }

    /** Public count; also tells a logged-in caller whether they liked it. */
    @GetMapping("/{recipeId}")
    public LikeSummaryDto getLikes(@PathVariable Long recipeId, @AuthenticationPrincipal Jwt jwt) {
        return likeService.getSummary(recipeId, CurrentUser.userIdOrNull(jwt));
    }
}
