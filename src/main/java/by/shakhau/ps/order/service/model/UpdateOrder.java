package by.shakhau.ps.order.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateOrder {

    private List<ProductSelect> createItems;
    private List<UpdateItem> updateItems;
}
