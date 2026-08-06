package by.shakhau.ps.order.repository;

import by.shakhau.ps.order.repository.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

    @Query(value = "SELECT oi FROM order_item oi WHERE io.userId = :orderId", nativeQuery = true)
    List<OrderItemEntity> findByOrderId(UUID orderId);
}
