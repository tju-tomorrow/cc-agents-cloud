package io.ccagents.cloud.auth;

import java.util.UUID;

public record RegisterResponse(UUID userId, String email, String displayName) {
}

