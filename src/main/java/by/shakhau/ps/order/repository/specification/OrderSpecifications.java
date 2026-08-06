package by.shakhau.ps.order.repository.specification;

import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSpecifications {

    public static Specification<OrderEntity> hasStatuses(Collection<OrderStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return cb.conjunction();
            }

            return root.get("status").in(statuses);
        };
    }

    public static Specification<OrderEntity> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
        };
    }

    public static Specification<OrderEntity> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }
}
