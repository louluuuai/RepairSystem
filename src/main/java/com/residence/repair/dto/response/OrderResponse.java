package com.residence.repair.dto.response;

import com.residence.repair.domain.enums.MediaType;
import com.residence.repair.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String description;
    private OrderStatus status;
    private Boolean entryAuthorized;
    private String entryNote;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;

    @Builder.Default
    private List<MediaResponse> mediaList = new ArrayList<>();

    @Data
    @Builder
    public static class MediaResponse {
        private String url;
        private MediaType type;
    }
}
