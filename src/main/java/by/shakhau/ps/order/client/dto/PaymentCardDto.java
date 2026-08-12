package by.shakhau.ps.order.client.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
public class PaymentCardDto {

    private final UUID id;
    private final String number;
    private final String holder;
    private final LocalDate expirationDate;
    private final Boolean active;
}
