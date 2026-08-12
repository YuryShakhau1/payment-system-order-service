package by.shakhau.ps.order.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentCardRequest {

    private String number;
    private String holder;
    private LocalDate expirationDate;
}
