package by.shakhau.ps.order.service.model;

import by.shakhau.ps.order.repository.entity.AuditableEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class OrderItem extends AuditableEntity {

    private UUID id;
    private OrderStatus status;
    private UUID itemId;
    private Long quantity;
    private BigDecimal itemPrice;
}
