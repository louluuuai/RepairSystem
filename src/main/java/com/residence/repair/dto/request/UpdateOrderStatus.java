package com.residence.repair.dto.request;

import com.residence.repair.domain.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatus {
    private OrderStatus orderStatus;
}
