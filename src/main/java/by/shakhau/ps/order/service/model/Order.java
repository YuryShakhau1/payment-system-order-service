package by.shakhau.ps.order.service.model;

import by.shakhau.ps.order.repository.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
public class Order {

    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private Boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItem> items;
}
