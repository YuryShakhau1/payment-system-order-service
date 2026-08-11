package by.shakhau.ps.order.service.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateItem {

    @NotNull(message = "Item id must not be empty")
    private UUID itemId;

    @NotNull(message = "Quantity must not be empty")
    @Positive(message = "Quantity must be greater 0")
    private Long quantity;
}
