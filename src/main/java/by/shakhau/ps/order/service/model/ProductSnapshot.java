package by.shakhau.ps.order.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductSnapshot {

    private UUID id;
    private UUID productId;
    private String name;
    private BigDecimal price;
}
