package io.ccagents.cloud.shared.error;

import java.util.Map;

public record ApiError(
        String code,
        String message,
        String requestId,
        Map<String, String> details) {
}

