package by.shakhau.ps.order.integration;

import by.shakhau.ps.order.repository.OrderItemRepository;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.UserRepository;
import by.shakhau.ps.order.service.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@WireMockTest(httpPort = 8080)
public abstract class AbstractIntegrationTest {

    private UUID currentUserId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine")
                    .withDatabaseName("test-db")
                    .withUsername("test-user")
                    .withPassword("test-password");

    static {
        postgres.start();
    }

    public UUID getCurrentUserId() {
        return currentUserId;
    }

    @BeforeEach
    public void setUp() throws Exception {
        currentUserId = UUID.randomUUID();

        var user = User.builder()
                .id(currentUserId)
                .email("john_doe@mail.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();

        stubFor(WireMock.get(urlPathEqualTo("/users/" + user.getId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(user))));
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
