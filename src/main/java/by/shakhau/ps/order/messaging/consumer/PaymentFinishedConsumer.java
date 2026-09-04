package by.shakhau.ps.order.messaging.consumer;

import by.shakhau.ps.order.messaging.event.PaymentFinishedEvent;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.OrderService;
import by.shakhau.ps.order.service.model.Actor;
import by.shakhau.ps.order.service.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentFinishedConsumer {

    private static final String TOPIC = "payment.finished";

    private final OrderService orderService;

    @KafkaListener(topics = TOPIC, groupId = "payment-service")
    public void consume(PaymentFinishedEvent event, Acknowledgment ack) {
        Order order = orderService.findById(event.getOrderId());

        if ("SUCCESS".equals(event.getPaymentStatus())) {
            orderService.updateStatus(order, OrderStatus.PAID, Actor.SYSTEM);
        } else {
            orderService.updateStatus(order, OrderStatus.PAYMENT_FAILED, Actor.SYSTEM);
        }

        ack.acknowledge();
    }
}
