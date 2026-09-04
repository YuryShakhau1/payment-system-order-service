package by.shakhau.ps.order.client;

import by.shakhau.ps.order.service.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "user-client", url = "${app.user-service-url}")
public interface UserClient {

    @GetMapping(
            value = "/users/{userId}",
            produces = APPLICATION_JSON_VALUE)
    User findUserById(@PathVariable UUID userId);
}