package com.openjar.socialservice.sevice;

import com.openjar.socialservice.dto.RecipeResponseDto;
import com.openjar.socialservice.dto.UserResponseDto;
import com.openjar.socialservice.timing.RequestTimingFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

/** Calls to sibling services, resolved through Eureka (load-balanced WebClient). */
@Slf4j
@Component
public class PlatformClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final WebClient.Builder webClientBuilder;
    private final String internalToken;

    public PlatformClient(WebClient.Builder webClientBuilder,
                          @Value("${openjar.internal-token:}") String internalToken) {
        this.webClientBuilder = webClientBuilder;
        this.internalToken = internalToken;
    }

    /**
     * Confirms the recipe exists. A 404 is surfaced to the caller; if recipe-service is merely
     * unreachable we degrade gracefully (empty) instead of blocking the interaction.
     */
    public Optional<RecipeResponseDto> findRecipe(Long recipeId) {
        try {
            return Optional.ofNullable(webClientBuilder.build().get()
                    .uri("http://recipe-service/recipe/{id}", recipeId)
                    .header(RequestTimingFilter.REQUEST_ID, requestId())
                    .retrieve()
                    .bodyToMono(RecipeResponseDto.class)
                    .block(TIMEOUT));
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found");
        } catch (Exception e) {
            log.warn("recipe-service lookup failed for {}: {}", recipeId, e.getMessage());
            return Optional.empty();
        }
    }

    /** Forward the current request id so the downstream service logs under the same id. */
    private static String requestId() {
        String id = MDC.get(RequestTimingFilter.MDC_KEY);
        return id != null ? id : "internal";
    }

    /** Private profile (with email) via user-service's internal, shared-secret endpoint. */
    public Optional<UserResponseDto> findUserWithEmail(String userId) {
        if (!StringUtils.hasText(internalToken)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(webClientBuilder.build().get()
                    .uri("http://user-service/internal/users/{id}", userId)
                    .header("X-Internal-Token", internalToken)
                    .header(RequestTimingFilter.REQUEST_ID, requestId())
                    .retrieve()
                    .bodyToMono(UserResponseDto.class)
                    .block(TIMEOUT));
        } catch (Exception e) {
            log.warn("user-service lookup failed for {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }
}
