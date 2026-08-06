package by.shakhau.ps.order.service.model;

import by.shakhau.ps.order.repository.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class Order {

    private UUID id;
    private UUID userId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private Boolean deleted;
}
