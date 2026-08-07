package by.shakhau.ps.order.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OrderItemChange {

    private OrderItem item;
    private long newQuantity;
}
