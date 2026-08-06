package by.shakhau.ps.order.service;

import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order findById(UUID id);
    List<Order> findByUserId(UUID userId);
    Page<Order> findInRange(
            LocalDateTime from, LocalDateTime to,
            Collection<OrderStatus> statuses,
            Boolean deleted,
            Pageable pageable);
    Order create(Order order);
    Order update(Order order);
    void updateDeleted(UUID orderId, Boolean deleted);
}
