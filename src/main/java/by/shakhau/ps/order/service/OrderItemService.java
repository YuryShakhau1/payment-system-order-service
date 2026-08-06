package by.shakhau.ps.order.service;

import by.shakhau.ps.order.service.model.OrderItem;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemService {

    OrderItem findById(UUID id);
    List<OrderItem> findByOrderId(UUID orderId);
    Collection<OrderItem> create(UUID orderId, Collection<OrderItem> orderItems);
    OrderItem update(OrderItem order);
}
