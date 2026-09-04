package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.PaymentCardClient;
import by.shakhau.ps.order.client.dto.PaymentCardDto;
import by.shakhau.ps.order.messaging.event.CreatePaymentEvent;
import by.shakhau.ps.order.messaging.producer.CreatePaymentProducer;
import by.shakhau.ps.order.service.Encryptor;
import by.shakhau.ps.order.service.OrderService;
import by.shakhau.ps.order.service.mapper.PaymentCardMapper;
import by.shakhau.ps.order.service.model.Actor;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.PaymentCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static by.shakhau.ps.order.repository.entity.OrderStatus.CREATED;
import static by.shakhau.ps.order.repository.entity.OrderStatus.PAYMENT_IN_PROCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceImplTest {

    @Mock
    private Encryptor encryptor;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private PaymentCardClient paymentCardClient;

    @Mock
    private OrderService orderService;

    @Mock
    private CreatePaymentProducer paymentProducer;

    @InjectMocks
    private OrderPaymentServiceImpl orderPaymentService;

    @Captor
    private ArgumentCaptor<CreatePaymentEvent> eventCaptor;

    private UUID userId;
    private UUID orderId;
    private UUID cardId;
    private String rawCvv;
    private String encryptedCvv;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        cardId = UUID.randomUUID();
        rawCvv = UUID.randomUUID().toString().substring(0, 3);
        encryptedCvv = "encrypted_" + rawCvv;
    }

    @Test
    void shouldFetchCardAndProceedWithPaymentWhenPayWithCardId() {
        var cardDto = PaymentCardDto.builder()
                .id(cardId)
                .number("4444")
                .holder("John Doe")
                .expirationDate(LocalDate.now().plusYears(2))
                .active(true)
                .build();

        var card = new PaymentCard();
        card.setNumber("4444");
        card.setHolder("John Doe");
        card.setExpirationDate(LocalDate.now().plusYears(2));

        var order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(CREATED);
        order.setTotalPrice(BigDecimal.valueOf(1000));

        when(paymentCardClient.findPaymentCard(cardId, userId)).thenReturn(cardDto);
        when(paymentCardMapper.toModel(cardDto)).thenReturn(card);
        when(orderService.findById(orderId)).thenReturn(order);
        when(orderService.updateStatus(order, PAYMENT_IN_PROCESS, Actor.SYSTEM))
                .thenReturn(Order.builder()
                        .id(orderId)
                        .items(order.getItems())
                        .userId(order.getUserId())
                        .status(PAYMENT_IN_PROCESS)
                        .build());
        when(encryptor.encrypt(any())).thenAnswer(invocation -> "encrypted_" + invocation.getArgument(0));

        Order result = orderPaymentService.pay(userId, orderId, cardId, rawCvv);

        assertNotNull(result);
        assertEquals(PAYMENT_IN_PROCESS, result.getStatus());

        verify(paymentCardClient).findPaymentCard(cardId, userId);
        verify(paymentCardMapper).toModel(cardDto);
        verify(orderService).updateStatus(order, PAYMENT_IN_PROCESS, Actor.SYSTEM);
        verify(paymentProducer).send(any(CreatePaymentEvent.class));
    }

    @Test
    void shouldEncryptDataAndSendEventWhenPayWithCardModel() {
        var card = new PaymentCard();
        card.setNumber("1111222233334444");
        card.setHolder("JOHN DOE");
        card.setExpirationDate(LocalDate.now().plusYears(1));

        var order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus(CREATED);
        order.setTotalPrice(BigDecimal.valueOf(550.50));

        when(orderService.findById(orderId)).thenReturn(order);
        when(encryptor.encrypt("1111222233334444")).thenReturn("card_number");
        when(encryptor.encrypt("JOHN DOE")).thenReturn("card_holder");
        when(encryptor.encrypt(rawCvv)).thenReturn(encryptedCvv);
        when(orderService.updateStatus(order, PAYMENT_IN_PROCESS, Actor.SYSTEM))
                .thenReturn(Order.builder()
                        .id(orderId)
                        .items(order.getItems())
                        .userId(order.getUserId())
                        .status(PAYMENT_IN_PROCESS)
                        .totalPrice(order.getTotalPrice())
                        .build());

        Order result = orderPaymentService.pay(orderId, card, rawCvv);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(PAYMENT_IN_PROCESS, result.getStatus());

        verify(orderService).findById(orderId);
        verify(orderService).updateStatus(order, PAYMENT_IN_PROCESS, Actor.SYSTEM);

        assertEquals("card_number", card.getNumber());
        assertEquals("card_holder", card.getHolder());

        verify(paymentProducer).send(eventCaptor.capture());
        CreatePaymentEvent sentEvent = eventCaptor.getValue();

        assertNotNull(sentEvent);
        assertEquals(orderId, sentEvent.getOrderId());
        assertEquals(userId, sentEvent.getUserId());
        assertEquals(BigDecimal.valueOf(550.50), sentEvent.getPaymentAmount());
        assertEquals("card_number", sentEvent.getCard().getNumber());
        assertEquals(encryptedCvv, sentEvent.getCvv());
    }
}
