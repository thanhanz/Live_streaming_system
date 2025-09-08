package com.thanhan.livestreaming_system.search_service.dto;

import java.util.Map;

public record SearchEvent(
        String type,
        String id,
        String action,
        Map<String, Object> payload
) {
}
