package by.shakhau.ps.order.repository;

import by.shakhau.ps.order.repository.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

    List<OrderEntity> findByUserId(UUID userId);

    @Query("UPDATE OrderEntity SET deleted = :deleted")
    @Modifying
    void updateDeleted(UUID id, Boolean deleted);
}
