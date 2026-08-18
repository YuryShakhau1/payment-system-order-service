package by.shakhau.ps.order.repository;

import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

    @EntityGraph(attributePaths = { "items", "items.productSnapshot" })
    Optional<OrderEntity> findById(UUID id);

    @EntityGraph(attributePaths = { "items" })
    Optional<OrderEntity> findByIdAndUserId(UUID id, UUID userId);

    @EntityGraph(attributePaths = { "items", "items.productSnapshot" })
    List<OrderEntity> findByUserId(UUID userId);

    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId")
    List<OrderEntity> findByUserIdWithoutItems(UUID userId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE orders SET status = :status WHERE id = :id AND status = :currentStatus", nativeQuery = true)
    int updateStatus(UUID id, int currentStatus, int status);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE orders SET deleted = :deleted WHERE id = :id", nativeQuery = true)
    void updateDeleted(UUID id, Boolean deleted);
}
