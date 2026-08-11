package by.shakhau.ps.order.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
public class ProductResponse {

    private UUID id;
    private String name;
    private BigDecimal price;
    private Boolean deleted;
}
