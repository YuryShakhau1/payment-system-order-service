package by.shakhau.ps.order.messaging.event;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserUpdatedEvent {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean active;
}
