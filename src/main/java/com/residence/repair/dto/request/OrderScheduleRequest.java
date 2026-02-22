package com.residence.repair.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class OrderScheduleRequest {
    @NotNull
    @Future
    private LocalDateTime scheduledAt;
}
