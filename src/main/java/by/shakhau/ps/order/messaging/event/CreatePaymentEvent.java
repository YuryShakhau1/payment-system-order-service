package by.shakhau.ps.order.messaging.event;

import by.shakhau.ps.order.service.model.PaymentCard;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
public class CreatePaymentEvent {

    private UUID orderId;
    private UUID userId;
    private BigDecimal paymentAmount;
    private PaymentCard card;
    private String cvv;
}
