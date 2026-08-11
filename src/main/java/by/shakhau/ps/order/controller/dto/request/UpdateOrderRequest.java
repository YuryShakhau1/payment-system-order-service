package by.shakhau.ps.order.controller.dto.request;

import by.shakhau.ps.order.service.model.ProductSelect;
import by.shakhau.ps.order.service.model.UpdateItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateOrderRequest {

    @NotNull
    @Size(max = 100)
    @Valid
    private List<ProductSelect> createItems;

    @NotNull
    @Size(max = 100)
    @Valid
    private List<@NotNull UpdateItem> updateItems;
}
