package com.residence.repair.dto.response;

import com.residence.repair.domain.enums.MediaType;
import lombok.Builder;
import lombok.Data;

/**
 * Réponse de téléversement média.
 */
@Data
@Builder
public class UploadMediaResponse {
    private String url;
    private MediaType type;
    private String fileName;
    private long size;
}
