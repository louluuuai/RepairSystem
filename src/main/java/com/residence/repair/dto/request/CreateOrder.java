package com.residence.repair.dto.request;

import com.residence.repair.domain.entity.Media;
import com.residence.repair.domain.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateOrder {
    @NotBlank
    private String description;
    @NotNull
    private Boolean entryAuthorized;
    private String entryNote;
    private List<Media> mediaList = new ArrayList<>();

    @Data
    public static class MediaRequest {
        @NotBlank
        private String url;

        @NotNull
        private MediaType type;
    }
}
