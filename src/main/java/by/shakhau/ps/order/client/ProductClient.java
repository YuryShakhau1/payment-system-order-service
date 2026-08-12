package by.shakhau.ps.order.client;

import by.shakhau.ps.order.client.dto.Product;
import by.shakhau.ps.order.client.dto.ProductIndices;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "product-client", url = "${app.product-service-url}")
public interface ProductClient {

    @PostMapping(value = "/products/filter", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    List<Product> findProducts(@RequestBody ProductIndices request);
}
