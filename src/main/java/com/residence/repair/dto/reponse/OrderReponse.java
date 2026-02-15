package com.residence.repair.dto.reponse;

import com.residence.repair.domain.entity.Media;
import com.residence.repair.domain.enums.MediaType;
import com.residence.repair.domain.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class OrderReponse {
    private Integer id;
    private String description;
    private OrderStatus status;
    private Boolean entryAuthorized;
    private String entryNote;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private List<Media> mediaList = new ArrayList<>();

    @Data
    @Builder
    public static class MediaResponse {
        private String url;
        private MediaType type;
    }
}
