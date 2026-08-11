package by.shakhau.ps.order.controller.dto.request;

import by.shakhau.ps.order.service.model.ProductSelect;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull
    @Size(min = 1, max = 100)
    @Valid
    private List<@NotNull ProductSelect> items;
}
