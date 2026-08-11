package by.shakhau.ps.order.service;

import by.shakhau.ps.order.service.model.OrderItem;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemService {

    List<OrderItem> findByOrderId(UUID orderId);
    void save(Collection<OrderItem> items);
    void deleteByIds(Collection<UUID> ids);
}
