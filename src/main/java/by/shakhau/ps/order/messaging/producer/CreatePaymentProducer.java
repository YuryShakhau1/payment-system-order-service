package by.shakhau.ps.order.messaging.producer;

import by.shakhau.ps.order.messaging.event.CreatePaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatePaymentProducer {

    private static final String TOPIC = "payment.create";
    private final KafkaTemplate<String, CreatePaymentEvent> template;

    public void send(CreatePaymentEvent event) {
        try {
            template.send(TOPIC, event.getOrderId().toString(), event).get();
        } catch (Exception e) {
            throw new KafkaException(e.getMessage(), e);
        }
    }
}
