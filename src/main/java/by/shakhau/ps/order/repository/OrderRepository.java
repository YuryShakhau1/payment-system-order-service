package by.shakhau.ps.order.repository;

import by.shakhau.ps.order.repository.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

    List<OrderEntity> findByUserId(UUID userId);


    List<OrderEntity> findInRange();
}
