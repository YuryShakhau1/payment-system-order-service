package by.shakhau.ps.order.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderItemUpdate {

    private final OrderItem item;
    private final UpdateItem updateItem;
}
