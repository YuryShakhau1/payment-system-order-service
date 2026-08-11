package by.shakhau.ps.order.repository.specification;

import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSpecifications {

    public static Specification<OrderEntity> hasStatuses(Collection<OrderStatus> statuses) {
        return (root, query, cb) ->
                Optional.ofNullable(statuses)
                        .map(s -> root.get("status").in(s))
                        .orElse(null);
    }

    public static Specification<OrderEntity> withUserId(UUID userId) {
        return (root, query, cb) ->
                Optional.ofNullable(userId)
                        .map(f -> cb.equal(root.get("userId"), f))
                        .orElse(null);
    }

    public static Specification<OrderEntity> createdAfter(LocalDateTime from) {
        return (root, query, cb) ->
                Optional.ofNullable(from)
                        .map(f -> cb.greaterThanOrEqualTo(root.get("createdAt"), f))
                        .orElse(null);
    }

    public static Specification<OrderEntity> createdBefore(LocalDateTime to) {
        return (root, query, cb) ->
                Optional.ofNullable(to)
                        .map(t -> cb.lessThanOrEqualTo(root.get("createdAt"), t))
                        .orElse(null);
    }

    public static Specification<OrderEntity> deleted(Boolean deleted) {
        return (root, query, cb) ->
                Optional.ofNullable(deleted)
                        .map(d -> cb.equal(root.get("deleted"), d))
                        .orElse(null);
    }
}
