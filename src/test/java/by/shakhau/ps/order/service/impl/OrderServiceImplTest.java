package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.ProductClient;
import by.shakhau.ps.order.client.dto.Product;
import by.shakhau.ps.order.client.dto.ProductIndices;
import by.shakhau.ps.order.exception.OperationForbiddenException;
import by.shakhau.ps.order.exception.ResourceNotFoundException;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.ProductSnapshotService;
import by.shakhau.ps.order.service.UserService;
import by.shakhau.ps.order.service.mapper.OrderMapper;
import by.shakhau.ps.order.service.model.Actor;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.OrderItem;
import by.shakhau.ps.order.service.model.ProductSelect;
import by.shakhau.ps.order.service.model.ProductSnapshot;
import by.shakhau.ps.order.service.model.UpdateItem;
import by.shakhau.ps.order.service.model.UpdateOrder;
import by.shakhau.ps.order.service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper mapper;

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductClient productClient;

    @Mock
    private UserService userService;

    @Mock
    private ProductSnapshotService productSnapshotService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private UUID orderId;
    private OrderEntity orderEntity;
    private Order orderModel;
    private User user;

    private UUID item1Id;
    private UUID item2Id;
    private UUID productId1;
    private UUID productId2;
    private OrderItem item1;
    private OrderItem item2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setUserId(userId);

        item1Id = UUID.randomUUID();
        item2Id = UUID.randomUUID();
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();

        item1 = OrderItem.builder().id(item1Id).productId(productId1).quantity(5L).itemPrice(BigDecimal.valueOf(10)).build();
        item2 = OrderItem.builder().id(item2Id).productId(productId2).quantity(3L).itemPrice(BigDecimal.valueOf(20)).build();

        orderModel = Order.builder()
                .id(orderId)
                .userId(userId)
                .status(OrderStatus.CREATED)
                .deleted(false)
                .items(new ArrayList<>(List.of(item1, item2)))
                .build();

        user = User.builder()
                .id(userId)
                .email("john_doe@mail.com")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void shouldReturnOrderModelWhenOrderExistsById() {
        when(repository.findById(orderId)).thenReturn(Optional.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        Order result = orderService.findById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(repository).findById(orderId);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenOrderDoesNotExistById() {
        when(repository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(orderId));
        verify(repository).findById(orderId);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldReturnOrderModelWhenOrderExistsByIdAndUserId() {
        when(repository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        Order result = orderService.findByIdAndUserId(orderId, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

    @Test
    void shouldCallFindByUserIdWhenWithItemsIsTrue() {
        when(repository.findByUserId(userId)).thenReturn(List.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        List<Order> result = orderService.findByUserId(userId, true);

        assertEquals(1, result.size());
        verify(repository).findByUserId(userId);
        verify(repository, never()).findByUserIdWithoutItems(any());
    }

    @Test
    void shouldCallFindByUserIdWithoutItemsWhenWithItemsIsFalse() {
        when(repository.findByUserIdWithoutItems(userId)).thenReturn(List.of(orderEntity));
        when(mapper.toModelWithoutItems(orderEntity)).thenReturn(orderModel);

        List<Order> result = orderService.findByUserId(userId, false);

        assertEquals(1, result.size());
        verify(repository).findByUserIdWithoutItems(userId);
        verify(repository, never()).findByUserId(any());
    }

    @Test
    void shouldReturnPageOfOrdersWhenFiltersAreApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> entityPage = new PageImpl<>(List.of(orderEntity));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        Page<Order> result = orderService.findFiltered(
                userId, LocalDateTime.now(), LocalDateTime.now(),
                List.of(OrderStatus.CREATED), false, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldReturnImmediatelyWithoutSavingWhenOrderStatusIsNotCreated() {
        orderModel.setStatus(OrderStatus.PAYMENT_IN_PROCESS);
        when(repository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        var updateOrder = new UpdateOrder();
        updateOrder.setCreateItems(List.of());
        updateOrder.setUpdateItems(List.of());

        Order result = orderService.update(userId, orderId, updateOrder);

        assertEquals(OrderStatus.PAYMENT_IN_PROCESS, result.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateQuantityDeleteMissingItemsAndRecalculatePriceWhenUpdatingOrder() {
        when(repository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        var updateItem1 = new UpdateItem();
        updateItem1.setId(item1Id);
        updateItem1.setQuantity(2L);

        UUID newProductId = UUID.randomUUID();
        ProductSelect newProductSelect = new ProductSelect(newProductId, 1L);

        UpdateOrder updateOrder = new UpdateOrder();
        updateOrder.setUpdateItems(List.of(updateItem1));
        updateOrder.setCreateItems(List.of(newProductSelect));

        var newProductResponse = new Product(newProductId, "New Item", BigDecimal.valueOf(50), false);
        when(productClient.findProducts(any(ProductIndices.class))).thenReturn(List.of(newProductResponse));

        when(mapper.toEntity(any(Order.class))).thenReturn(orderEntity);
        when(repository.save(orderEntity)).thenReturn(orderEntity);
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        Order result = orderService.update(userId, orderId, updateOrder);

        assertNotNull(result);
        assertEquals(2L, item1.getQuantity());
        assertEquals(2, orderModel.getItems().size());
        assertEquals(BigDecimal.valueOf(70), orderModel.getTotalPrice());

        verify(repository).save(orderEntity);
    }

    @Test
    void shouldUpdateStatusSuccessfullyWhenTransitionIsValid() {
        orderModel.setStatus(OrderStatus.CREATED);

        when(repository.updateStatus(orderId, OrderStatus.CREATED.getValue(), OrderStatus.CANCELLED.getValue()))
                .thenReturn(1);
        when(userService.fetchById(userId)).thenReturn(user);

        Order result = orderService.updateStatus(orderModel, OrderStatus.CANCELLED, Actor.USER);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(repository).updateStatus(orderId, OrderStatus.CREATED.getValue(), OrderStatus.CANCELLED.getValue());
    }

    @Test
    void shouldThrowOperationForbiddenExceptionWhenTransitionIsInvalid() {
        orderModel.setStatus(OrderStatus.CREATED);

        assertThrows(OperationForbiddenException.class, () ->
                orderService.updateStatus(orderModel, OrderStatus.PAID, Actor.USER)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldThrowOperationForbiddenExceptionWhenConcurrentUpdateFails() {
        when(repository.updateStatus(orderId, OrderStatus.CREATED.getValue(), OrderStatus.CANCELLED.getValue()))
                .thenReturn(0);

        assertThrows(OperationForbiddenException.class, () ->
                orderService.updateStatus(orderModel, OrderStatus.CANCELLED, Actor.USER));
    }

    @Test
    void shouldSetDeletedToTrueWhenStatusChangesToDeleted() {
        orderModel.setStatus(OrderStatus.CANCELLED);

        when(repository.updateStatus(orderId, OrderStatus.CANCELLED.getValue(), OrderStatus.DELETED.getValue()))
                .thenReturn(1);
        when(userService.fetchById(userId)).thenReturn(user);

        orderService.updateStatus(orderModel, OrderStatus.DELETED, Actor.ADMIN);

        verify(repository).updateDeleted(orderId, true);
    }

    @Test
    void shouldSetDeletedToFalseWhenStatusChangesFromDeletedToCancelled() {
        orderModel.setStatus(OrderStatus.DELETED);

        when(repository.updateStatus(orderId, OrderStatus.DELETED.getValue(), OrderStatus.CANCELLED.getValue()))
                .thenReturn(1);
        when(userService.fetchById(userId)).thenReturn(user);

        orderService.updateStatus(orderModel, OrderStatus.CANCELLED, Actor.ADMIN);

        verify(repository).updateDeleted(orderId, false);
    }

    @Test
    void shouldFindOrderAndThenUpdateStatus() {
        orderModel.setStatus(OrderStatus.CREATED);

        when(repository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);
        when(repository.updateStatus(orderId, OrderStatus.CREATED.getValue(), OrderStatus.CANCELLED.getValue()))
                .thenReturn(1);
        when(userService.fetchById(userId)).thenReturn(user);

        Order result = orderService.updateStatus(userId, orderId, OrderStatus.CANCELLED, Actor.USER);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(repository).findByIdAndUserId(orderId, userId);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        var newProductSnapshotId = UUID.randomUUID();
        var newProductId = UUID.randomUUID();
        var productSelect = new ProductSelect(newProductId, 2L);
        List<ProductSelect> selects = List.of(productSelect);
        var productResponse = new Product(newProductId, "Test Product", BigDecimal.valueOf(100), false);
        var productSnapshot = ProductSnapshot.builder()
                .id(newProductSnapshotId)
                .productId(newProductId)
                .name("iPhone")
                .price(new BigDecimal("999"))
                .build();

        when(productClient.findProducts(any(ProductIndices.class))).thenReturn(List.of(productResponse));
        when(productSnapshotService.fetchByProductIds(List.of(newProductId)))
                .thenReturn(Map.of(newProductId, productSnapshot));
        when(mapper.toEntity(any(Order.class))).thenReturn(orderEntity);
        when(repository.save(orderEntity)).thenReturn(orderEntity);
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);
        when(userService.fetchById(userId)).thenReturn(user);

        Order result = orderService.create(userId, selects);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(userId, result.getUserId());

        verify(repository).save(any(OrderEntity.class));
        verify(userService).fetchById(userId);
    }
}
