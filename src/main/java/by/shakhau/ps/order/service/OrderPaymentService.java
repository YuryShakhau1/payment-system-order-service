package by.shakhau.ps.order.service;

import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.PaymentCard;

import java.util.UUID;

public interface OrderPaymentService {

    Order pay(UUID userId, UUID orderId, UUID cardId, String cvv);
    Order pay(UUID orderId, PaymentCard card, String cvv);
}
