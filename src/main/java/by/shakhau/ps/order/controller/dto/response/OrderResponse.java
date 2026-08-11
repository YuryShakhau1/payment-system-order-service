package by.shakhau.ps.order.controller.dto.response;

import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.model.OrderItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private Boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItem> items;
}
