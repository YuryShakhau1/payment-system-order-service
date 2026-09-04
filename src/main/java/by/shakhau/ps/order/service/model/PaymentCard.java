package by.shakhau.ps.order.service.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentCard {

    private String number;
    private String holder;
    private LocalDate expirationDate;
}
