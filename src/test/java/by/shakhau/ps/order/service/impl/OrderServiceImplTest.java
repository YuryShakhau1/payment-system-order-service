package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.ProductClient;
import by.shakhau.ps.order.client.dto.Product;
import by.shakhau.ps.order.client.dto.ProductIndices;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.service.exception.ResourceNotFoundException;
import by.shakhau.ps.order.service.mapper.OrderMapper;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.OrderItem;
import by.shakhau.ps.order.service.model.ProductSelect;
import by.shakhau.ps.order.service.model.UpdateItem;
import by.shakhau.ps.order.service.model.UpdateOrder;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doNothing;
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

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userId;
    private UUID orderId;
    private OrderEntity orderEntity;
    private Order orderModel;

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
        orderModel.setStatus(OrderStatus.PENDING_PAYMENT);
        when(repository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        var updateOrder = new UpdateOrder();
        updateOrder.setCreateItems(List.of());
        updateOrder.setUpdateItems(List.of());

        Order result = orderService.update(userId, orderId, updateOrder);

        assertEquals(OrderStatus.PENDING_PAYMENT, result.getStatus());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldUpdateQuantityDeleteMissingItemsAndRecalculatePriceWhenUpdatingOrder() {
        when(repository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(orderEntity));
        when(mapper.toModel(orderEntity)).thenReturn(orderModel);

        var updateItem1 = new UpdateItem();
        updateItem1.setItemId(item1Id);
        updateItem1.setQuantity(2L);

        UUID newProductId = UUID.randomUUID();
        ProductSelect newProductSelect = new ProductSelect(newProductId, 1L);

        UpdateOrder updateOrder = new UpdateOrder();
        updateOrder.setUpdateItems(List.of(updateItem1));
        updateOrder.setCreateItems(List.of(newProductSelect));

        var newProductResponse = new Product(newProductId, "New Item", BigDecimal.valueOf(50), false);
        when(productClient.findProducts(any(ProductIndices.class))).thenReturn(List.of(newProductResponse));

        when(mapper.toEntity(anyBoolean(), any(Order.class))).thenReturn(orderEntity);
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
    void shouldCallRepositoryUpdateDeletedWhenInvoked() {
        doNothing().when(repository).updateDeleted(orderId, true);

        orderService.updateDeleted(orderId, true);

        verify(repository).updateDeleted(orderId, true);
    }
}
