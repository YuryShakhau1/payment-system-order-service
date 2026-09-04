package by.shakhau.ps.order.service.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@EqualsAndHashCode(of = { "orderId", "productId", "quantity" }, callSuper = false)
public class OrderItem {

    private UUID id;
    private UUID orderId;
    private UUID productId;
    private Long quantity;
    private BigDecimal itemPrice;
    private ProductSnapshot productSnapshot;
}
