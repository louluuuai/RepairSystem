package com.residence.repair.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Réponse contenant les tokens JWT.
 */
@Data
@Builder
public class TokenResponse {
    private String accessToken;
}