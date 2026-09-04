package by.shakhau.ps.order.integration;

import by.shakhau.ps.order.client.dto.Product;
import by.shakhau.ps.order.controller.dto.request.CreateOrderRequest;
import by.shakhau.ps.order.controller.dto.request.ItemDto;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static by.shakhau.ps.order.controller.filter.AuthenticationFilter.SESSION_ID_HEADER;
import static by.shakhau.ps.order.controller.filter.AuthenticationFilter.USER_ID_HEADER;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        var product = new Product(productId, "Test Product", BigDecimal.valueOf(50), false);

        stubFor(WireMock.post(urlPathEqualTo("/products/filter"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(List.of(product)))));

        var request = new CreateOrderRequest();
        request.setItems(List.of(new ItemDto(productId, 2L)));

        mockMvc.perform(post("/orders")
                        .header(USER_ID_HEADER, getCurrentUserId())
                        .header(SESSION_ID_HEADER, UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.totalPrice").value(100.0));
    }
}
