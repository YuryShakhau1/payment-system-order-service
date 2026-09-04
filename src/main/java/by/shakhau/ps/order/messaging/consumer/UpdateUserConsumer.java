package by.shakhau.ps.order.messaging.consumer;

import by.shakhau.ps.order.messaging.event.UserUpdatedEvent;
import by.shakhau.ps.order.messaging.mapper.UserEventMapper;
import by.shakhau.ps.order.service.SaveUserService;
import by.shakhau.ps.order.service.model.User;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UpdateUserConsumer {

    private static final String TOPIC = "user.updated";

    private final UserEventMapper userEventMapper;
    private final SaveUserService saveUserService;

    @KafkaListener(topics = TOPIC, groupId = "order-service")
    public void consume(UserUpdatedEvent event, Acknowledgment ack) {
        User user = userEventMapper.toUser(event);
        saveUserService.save(user);

        ack.acknowledge();
    }
}
