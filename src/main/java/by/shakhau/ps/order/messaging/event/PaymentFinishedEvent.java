package by.shakhau.ps.order.messaging.event;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PaymentFinishedEvent {

    private UUID paymentId;
    private UUID orderId;
    private String paymentStatus;
}
