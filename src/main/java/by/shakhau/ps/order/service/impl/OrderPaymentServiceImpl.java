package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.PaymentCardClient;
import by.shakhau.ps.order.client.dto.PaymentCardDto;
import by.shakhau.ps.order.messaging.event.CreatePaymentEvent;
import by.shakhau.ps.order.messaging.producer.CreatePaymentProducer;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.Encryptor;
import by.shakhau.ps.order.service.OrderPaymentService;
import by.shakhau.ps.order.service.OrderService;
import by.shakhau.ps.order.service.mapper.PaymentCardMapper;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.PaymentCard;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentServiceImpl implements OrderPaymentService {

    private final Encryptor encryptor;
    private final PaymentCardMapper paymentCardMapper;
    private final PaymentCardClient paymentCardClient;
    private final OrderService orderService;
    private final CreatePaymentProducer paymentProducer;

    @Transactional
    @Override
    public Order pay(UUID userId, UUID orderId, UUID cardId, String cvv) {
        PaymentCardDto paymentCard = paymentCardClient.findPaymentCard(cardId, userId);
        return pay(orderId, paymentCardMapper.toModel(paymentCard), cvv);
    }

    @Transactional
    @Override
    public Order pay(UUID orderId, PaymentCard card, String cvv) {
        Order order = orderService.findById(orderId);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        orderService.updateStatus(order, OrderStatus.PENDING_PAYMENT);

        card.setNumber(encryptor.encrypt(card.getNumber()));
        card.setHolder(encryptor.encrypt(card.getHolder()));

        paymentProducer.send(CreatePaymentEvent.builder()
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .paymentAmount(order.getTotalPrice())
                        .card(card)
                        .cvv(encryptor.encrypt(cvv))
                .build());

        return order;
    }
}
