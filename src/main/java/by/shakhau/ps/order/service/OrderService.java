package by.shakhau.ps.order.service;

import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.ProductSelect;
import by.shakhau.ps.order.service.model.UpdateOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order findById(UUID id);
    Order findByIdAndUserId(UUID id, UUID userId);
    List<Order> findByUserId(UUID userId, boolean withItems);
    Page<Order> findFiltered(
            UUID userId,
            LocalDateTime from, LocalDateTime to,
            Collection<OrderStatus> statuses,
            Boolean deleted,
            Pageable pageable);
    Order create(UUID userId, List<ProductSelect> selects);
    Order update(UUID userId, UUID orderId, UpdateOrder updateOrder);
    void updateDeleted(UUID id, Boolean deleted);
}
