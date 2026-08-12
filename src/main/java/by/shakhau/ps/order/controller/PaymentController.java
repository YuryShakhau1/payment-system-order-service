package by.shakhau.ps.order.controller;

import by.shakhau.ps.order.controller.dto.mapper.OrderDtoMapper;
import by.shakhau.ps.order.controller.dto.mapper.PaymentCardDtoMapper;
import by.shakhau.ps.order.controller.dto.request.PaymentCardRequest;
import by.shakhau.ps.order.controller.dto.response.OrderResponse;
import by.shakhau.ps.order.controller.filter.AuthenticationFilter.UserPrincipal;
import by.shakhau.ps.order.service.OrderPaymentService;
import by.shakhau.ps.order.service.model.PaymentCard;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/orders/{orderId}/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderDtoMapper orderMapper;
    private final PaymentCardDtoMapper paymentCardDtoMapper;
    private final OrderPaymentService orderPaymentService;

    @PostMapping(value = "/cards/{cardId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> pay(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID orderId,
            @PathVariable UUID cardId,
            @RequestParam String cvv) {
        UUID userId = userPrincipal.getId();
        return ResponseEntity.ok(orderMapper.toDto(orderPaymentService.pay(userId, orderId, cardId, cvv)));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> payWithExternalCard(
            @PathVariable UUID orderId,
            @RequestParam String cvv,
            @RequestBody PaymentCardRequest request) {
        PaymentCard card = paymentCardDtoMapper.toModel(request);
        return ResponseEntity.ok(orderMapper.toDto(orderPaymentService.pay(orderId, card, cvv)));
    }
}
