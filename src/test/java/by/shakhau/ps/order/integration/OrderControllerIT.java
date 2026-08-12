package by.shakhau.ps.order.integration;

import by.shakhau.ps.order.client.ProductClient;
import by.shakhau.ps.order.client.dto.Product;
import by.shakhau.ps.order.client.dto.ProductIndices;
import by.shakhau.ps.order.controller.dto.request.CreateOrderRequest;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.model.ProductSelect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static by.shakhau.ps.order.controller.filter.AuthenticationFilter.SESSION_ID_HEADER;
import static by.shakhau.ps.order.controller.filter.AuthenticationFilter.USER_ID_HEADER;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
        mockMvc.perform(get("/orders/{id}/me", orderId)
                        .header(USER_ID_HEADER, UUID.randomUUID())
                        .header(SESSION_ID_HEADER, UUID.randomUUID())
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
                        .header(USER_ID_HEADER, getCurrentUserId())
                        .header(SESSION_ID_HEADER, UUID.randomUUID())
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

        Product product = new Product(productId, "Test Product", BigDecimal.valueOf(50), false);
        when(productClient.findProducts(any(ProductIndices.class))).thenReturn(List.of(product));

        var request = new CreateOrderRequest();
        request.setItems(List.of(new ProductSelect(productId, 2L)));

        mockMvc.perform(post("/orders")
                        .header(USER_ID_HEADER, getCurrentUserId())
                        .header(SESSION_ID_HEADER, UUID.randomUUID())
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
