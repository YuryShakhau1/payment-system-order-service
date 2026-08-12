package by.shakhau.ps.order.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum OrderStatus {

    CREATED(0),
    PENDING_PAYMENT(1),
    PAID(2),
    PAYMENT_FAILED(3),
    IN_DELIVERY(4),
    COMPLETED(5),
    CANCELLED(6),
    REFUNDED(7);

    private final int value;

    private static final Map<Integer, OrderStatus> STATUSES = Arrays.stream(values())
            .collect(Collectors.toMap(OrderStatus::getValue, os -> os));

    public static OrderStatus fromValue(int value) {
        return STATUSES.get(value);
    }

    public static OrderStatus getBeginStatus() {
        return CREATED;
    }
}
