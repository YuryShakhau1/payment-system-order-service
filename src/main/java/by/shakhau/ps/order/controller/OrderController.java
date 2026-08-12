package by.shakhau.ps.order.controller;

import by.shakhau.ps.order.controller.dto.mapper.OrderDtoMapper;
import by.shakhau.ps.order.controller.dto.request.CreateOrderRequest;
import by.shakhau.ps.order.controller.dto.request.UpdateOrderRequest;
import by.shakhau.ps.order.controller.dto.response.OrderResponse;
import by.shakhau.ps.order.controller.filter.AuthenticationFilter.UserPrincipal;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.OrderService;
import by.shakhau.ps.order.service.exception.ResourceForbiddenException;
import by.shakhau.ps.order.service.model.Order;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderDtoMapper mapper;
    private final OrderService service;

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> findOrderById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(service.findById(id)));
    }

    @GetMapping(value = "/{id}/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> findCurrentUserOrderById(
            @AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable UUID id) {
        Order order = service.findById(id);
        if (!userPrincipal.getId().equals(order.getUserId())) {
            throw new ResourceForbiddenException("Resource forbidden");
        }

        return ResponseEntity.ok(mapper.toDto(order));
    }

    @GetMapping(value = "/filtered", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<OrderResponse>> findByFilter(
            @RequestParam(required = false) UUID userId,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestParam(required = false) Collection<OrderStatus> statuses,
            @RequestParam(required = false) Boolean deleted,
            Pageable pageable) {
        return ResponseEntity.ok(service.findFiltered(
                userId, from, to, statuses, deleted, pageable).map(mapper::toDto));
    }

    @GetMapping(value = "/me/filtered", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<OrderResponse>> findCurrentUserByFilter(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to,
            @RequestParam(required = false) Collection<OrderStatus> statuses,
            @RequestParam(required = false) Boolean deleted,
            Pageable pageable) {
        UUID userId = userPrincipal.getId();
        return ResponseEntity.ok(service.findFiltered(
                userId, from, to, statuses, deleted, pageable).map(mapper::toDto));
    }

    @GetMapping(value = "/users/{userId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponse>> findByUserId(@PathVariable UUID userId) {
        List<OrderResponse> orders = service.findByUserId(userId, false).stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<OrderResponse>> findByCurrentUserId(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID userId = userPrincipal.getId();
        List<OrderResponse> orders = service.findByUserId(userId, false).stream()
                .map(mapper::toDto)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateOrderRequest request) {
        UUID userId = userPrincipal.getId();
        OrderResponse order = mapper.toDto(service.create(userId, request.getItems()));
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<OrderResponse> updateCurrentUserOrder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderRequest request) {
        UUID userId = userPrincipal.getId();
        Order order = service.update(userId, id, mapper.toModel(request));
        return ResponseEntity.ok(mapper.toDto(order));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<Void> restoreOrder(@PathVariable UUID id) {
        service.updateDeleted(id, false);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        service.updateDeleted(id, true);
        return ResponseEntity.noContent().build();
    }
}
