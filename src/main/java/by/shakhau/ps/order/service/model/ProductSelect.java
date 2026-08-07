package by.shakhau.ps.order.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductSelect {

    @NotNull(message = "Product id must not be empty")
    private UUID productId;

    @NotNull(message = "Quantity must not be empty")
    @Positive(message = "Quantity must be greater 0")
    private Long quantity;
}
