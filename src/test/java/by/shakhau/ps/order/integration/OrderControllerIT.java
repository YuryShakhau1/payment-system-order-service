package by.shakhau.ps.order.integration;

import by.shakhau.ps.order.client.ProductClient;
import by.shakhau.ps.order.client.dto.ProductIdsRequest;
import by.shakhau.ps.order.client.dto.ProductResponse;
import by.shakhau.ps.order.controller.dto.request.CreateOrderRequest;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.model.ProductSelect;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductClient productClient;

    private UUID orderId;
    private OrderEntity savedOrder;

    @BeforeEach
    void setUpCurrent() {
        OrderEntity order = new OrderEntity();
        order.setUserId(getCurrentUserId());
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100));
        order.setDeleted(false);
        order.setItems(new ArrayList<>());

        savedOrder = orderRepository.save(order);
        orderId = savedOrder.getId();
    }

    @Test
    void shouldReturnOrderWhenUserIsOwner() throws Exception {
        mockMvc.perform(get("/orders/{id}", orderId)
                        .header(AUTHORIZATION, AUTHORIZATION_HEADER)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(getCurrentUserId().toString()));
    }

    @Test
    void shouldReturnForbiddenWhenUserIsNotOwnerAndNotAdmin() throws Exception {
        Claims strangerClaims = mock(Claims.class);

        when(strangerClaims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 100000));
        UUID strangerUserId = UUID.randomUUID();
        when(strangerClaims.getSubject()).thenReturn(strangerUserId.toString());
        when(strangerClaims.get("roles")).thenReturn(Collections.singletonList("ROLE_USER"));
        when(jwtService.getClaims(any())).thenReturn(strangerClaims);

        mockMvc.perform(get("/orders/{id}/me", orderId)
                        .header(AUTHORIZATION, AUTHORIZATION_HEADER)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnOrderWhenUserIsAdminButNotOwner() throws Exception {
        mockMvc.perform(get("/orders/{id}", orderId)
                        .header(AUTHORIZATION, AUTHORIZATION_HEADER)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void shouldReturnFilteredOrdersForCurrentUser() throws Exception {
        mockMvc.perform(get("/orders/me/filtered")
                        .header(AUTHORIZATION, AUTHORIZATION_HEADER)
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-12-31T23:59:59")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(orderId.toString()));
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        UUID productId = UUID.randomUUID();

        ProductResponse productResponse = new ProductResponse(productId, "Test Product", BigDecimal.valueOf(50), false);
        when(productClient.findProducts(any(ProductIdsRequest.class))).thenReturn(List.of(productResponse));

        var request = new CreateOrderRequest();
        request.setItems(List.of(new ProductSelect(productId, 2L)));

        mockMvc.perform(post("/orders")
                        .header(AUTHORIZATION, AUTHORIZATION_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.totalPrice").value(100.0));
    }

    @Test
    void shouldSoftDeleteOrderSuccessfully() throws Exception {
        mockMvc.perform(delete("/orders/{id}", orderId)
                        .header(AUTHORIZATION, AUTHORIZATION_HEADER))
                .andExpect(status().isNoContent());

        OrderEntity updatedEntity = orderRepository.findById(orderId).orElseThrow();
        assertTrue(updatedEntity.getDeleted());
    }

    @Test
    void shouldRestoreOrderSuccessfully() throws Exception {
        savedOrder.setDeleted(true);
        orderRepository.save(savedOrder);

        mockMvc.perform(patch("/orders/{id}/restore", orderId)
                        .header(AUTHORIZATION, AUTHORIZATION_HEADER))
                .andExpect(status().isNoContent());

        OrderEntity updatedEntity = orderRepository.findById(orderId).orElseThrow();
        assertFalse(updatedEntity.getDeleted());
    }
}
