package by.shakhau.ps.order.integration;

import by.shakhau.ps.order.controller.dto.mapper.OrderDtoMapper;
import by.shakhau.ps.order.controller.dto.mapper.PaymentCardDtoMapper;
import by.shakhau.ps.order.controller.dto.request.PaymentCardRequest;
import by.shakhau.ps.order.controller.dto.response.OrderResponse;
import by.shakhau.ps.order.controller.filter.AuthenticationFilter.UserPrincipal;
import by.shakhau.ps.order.service.OrderPaymentService;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.PaymentCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PaymentControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderDtoMapper orderMapper;

    @MockitoBean
    private PaymentCardDtoMapper paymentCardDtoMapper;

    @MockitoBean
    private OrderPaymentService orderPaymentService;

    private UUID userId;
    private UUID orderId;
    private UUID cardId;
    private String cvv;
    private UserPrincipal userPrincipal;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUpPayment() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        cardId = UUID.randomUUID();
        cvv = UUID.randomUUID().toString().substring(0, 3);

        userPrincipal = new UserPrincipal(userId, null);

        order = new Order();
        orderResponse = new OrderResponse();
    }

    @Test
    void shouldReturnOrderResponseWhenPayWithSavedCard() throws Exception {
        when(orderPaymentService.pay(userId, orderId, cardId, cvv)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        mockMvc.perform(post("/orders/{orderId}/pay", orderId)
                        .param("cvv", cvv)
                        .param("cardId", cardId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal)))
                .andExpect(status().isOk());

        verify(orderPaymentService).pay(userId, orderId, cardId, cvv);
        verify(orderMapper).toDto(order);
    }

    @Test
    void shouldReturnOrderResponseWhenPayWithExternalCard() throws Exception {
        var request = new PaymentCardRequest();

        var card = new PaymentCard();

        when(paymentCardDtoMapper.toModel(any(PaymentCardRequest.class))).thenReturn(card);
        when(orderPaymentService.pay(orderId, card, cvv)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        mockMvc.perform(post("/orders/{orderId}/pay/external-card", orderId)
                        .param("cvv", cvv)
                        .param("cardId", cardId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(paymentCardDtoMapper).toModel(any(PaymentCardRequest.class));
        verify(orderPaymentService).pay(orderId, card, cvv);
        verify(orderMapper).toDto(order);
    }
}
