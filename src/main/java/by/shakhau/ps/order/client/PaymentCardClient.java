package by.shakhau.ps.order.client;

import by.shakhau.ps.order.client.dto.PaymentCardDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "payment-card-client", url = "${app.user-service-url}")
public interface PaymentCardClient {

    @GetMapping(
            value = "/users/payment-cards/{cardId}",
            produces = APPLICATION_JSON_VALUE)
    PaymentCardDto findPaymentCard(@PathVariable UUID cardId, @RequestParam UUID userId);
}