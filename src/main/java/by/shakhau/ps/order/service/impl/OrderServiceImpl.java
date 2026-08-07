package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.ProductClient;
import by.shakhau.ps.order.client.dto.ProductIdsRequest;
import by.shakhau.ps.order.client.dto.ProductResponse;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.repository.specification.OrderSpecifications;
import by.shakhau.ps.order.service.OrderItemService;
import by.shakhau.ps.order.service.OrderService;
import by.shakhau.ps.order.service.exception.ResourceNotFoundException;
import by.shakhau.ps.order.service.mapper.OrderItemMapper;
import by.shakhau.ps.order.service.mapper.OrderMapper;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.OrderItem;
import by.shakhau.ps.order.service.model.OrderItemUpdate;
import by.shakhau.ps.order.service.model.ProductSelect;
import by.shakhau.ps.order.service.model.UpdateItem;
import by.shakhau.ps.order.service.model.UpdateOrder;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper mapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderRepository repository;
    private final ProductClient productClient;
    private final OrderItemService orderItemService;

    @Override
    public Order findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id = %s not found".formatted(id)));
    }

    @Override
    public Order findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(mapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id = %s not found".formatted(id)));
    }

    @Override
    public List<Order> findByUserId(UUID userId, boolean withItems) {
        if (!withItems) {
            return repository.findByUserIdWithoutItems(userId).stream()
                    .map(mapper::toModelWithoutItems)
                    .toList();
        }

        return repository.findByUserId(userId).stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public Page<Order> findInRange(
            LocalDateTime from, LocalDateTime to,
            Collection<OrderStatus> statuses,
            Boolean deleted,
            Pageable pageable) {
        Specification<OrderEntity> specification = Specification.allOf(
                OrderSpecifications.createdAfter(from),
                OrderSpecifications.createdBefore(to),
                OrderSpecifications.hasStatuses(statuses),
                OrderSpecifications.deleted(deleted));

        return repository.findAll(specification, pageable).map(mapper::toModel);
    }

    @Retryable(
            retryFor = {FeignException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2))
    @Transactional
    @Override
    public Order create(UUID userId, List<ProductSelect> selects) {
        List<ProductSelect> mergedSelects = mergeSelectsByProducts(selects);
        Map<UUID, ProductResponse> productMap = findProducts(mergedSelects);
        List<OrderItem> items = createOrderItems(selects, productMap);

        var order = Order.builder()
                .userId(userId)
                .status(OrderStatus.getBeginStatus())
                .totalPrice(calcTotalPrice(items))
                .items(items)
                .deleted(false)
                .build();

        return mapper.toModel(repository.save(mapper.toEntity(false, order)));
    }

    @Transactional
    @Override
    public Order update(UUID userId, UUID orderId, UpdateOrder updateOrder) {
        Order order = findByIdAndUserId(orderId, userId);
        if (order.getStatus() != OrderStatus.CREATED) {
            return order;
        }

        Map<UUID, UpdateItem> updateItems = updateOrder.getUpdateItems().stream()
                .collect(Collectors.toMap(UpdateItem::getItemId, i -> i));

        List<OrderItem> items = order.getItems();
        List<OrderItem> itemsToSave = new ArrayList<>(items.size());
        List<OrderItemUpdate> orderItemUpdates = new ArrayList<>();
        for (OrderItem item : items) {
            UpdateItem updateItem = updateItems.get(item.getId());
            if (updateItem == null) {
                continue;
            }

            if (updateItem.getQuantity() < item.getQuantity()) {
                item.setQuantity(updateItem.getQuantity());
            } else if (updateItem.getQuantity() > item.getQuantity()) {
                orderItemUpdates.add(new OrderItemUpdate(item, updateItem));
            }

            itemsToSave.add(item);
        }

        List<ProductSelect> createItems = new ArrayList<>(updateOrder.getCreateItems());
        Set<UUID> productIds = createItems.stream()
                .map(ProductSelect::getProductId)
                .collect(Collectors.toSet());
        productIds.addAll(orderItemUpdates.stream()
                .map(oic -> oic.getItem().getProductId())
                .toList());

        Map<UUID, ProductResponse> productMap = findProductsByIds(productIds);

        createItems.addAll(orderItemUpdates.stream()
                .map(update -> applyUpdates(productMap, update))
                .filter(Objects::nonNull)
                .toList());

        itemsToSave.addAll(createOrderItems(createItems, productMap));

        order.setItems(itemsToSave);
        order.setTotalPrice(calcTotalPrice(order.getItems()));

        return mapper.toModel(repository.save(mapper.toEntity(order.getDeleted(), order)));
    }

    private ProductSelect applyUpdates(Map<UUID, ProductResponse> productMap, OrderItemUpdate orderItemUpdate) {
        OrderItem item = orderItemUpdate.getItem();
        UpdateItem updateItem = orderItemUpdate.getUpdateItem();
        if (updateItem.getQuantity() > item.getQuantity()) {
            ProductResponse product = productMap.get(item.getProductId());
            if (product.getDeleted()) {
                return null;
            }

            if (item.getItemPrice().equals(product.getPrice())) {
                item.setQuantity(updateItem.getQuantity());
                return null;
            }

            return new ProductSelect(product.getId(), updateItem.getQuantity() - item.getQuantity());
        }

        return null;
    }

    private List<OrderItem> createOrderItems(
            List<ProductSelect> selects, Map<UUID, ProductResponse> productMap) {
        return selects.stream()
                .map(ps -> Optional.ofNullable(productMap.get(ps.getProductId()))
                        .map(product -> {
                            UUID productId = product.getId();
                            return OrderItem.builder()
                                    .productId(productId)
                                    .quantity(ps.getQuantity())
                                    .itemPrice(product.getPrice())
                                    .build();
                        })
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<ProductSelect> mergeSelectsByProducts(List<ProductSelect> selects) {
        List<ProductSelect> mergedSelects = new ArrayList<>(selects.size());
        Map<UUID, List<ProductSelect>> productSelectsMap = selects.stream()
                .collect(Collectors.groupingBy(ProductSelect::getProductId));

        for (ProductSelect select : selects) {
            List<ProductSelect> productSelects = productSelectsMap.get(select.getProductId());
            mergedSelects.add(mergeSelects(productSelects));
        }

        return mergedSelects;
    }

    private ProductSelect mergeSelects(List<ProductSelect> selects) {
        ProductSelect firstSelect = selects.getFirst();
        if (selects.size() == 1) {
            return firstSelect;
        }

        long totalQuantity = selects.stream()
                .mapToLong(ProductSelect::getQuantity)
                .sum();
        firstSelect.setQuantity(totalQuantity);
        return firstSelect;
    }

    @Transactional
    @Override
    public void updateDeleted(UUID id, Boolean deleted) {
        repository.updateDeleted(id, deleted);
    }

    private Map<UUID, ProductResponse> findProductsByIds(Collection<UUID> productIds) {
        var productsRequest = new ProductIdsRequest(productIds);
        return productClient.findProducts(productsRequest).stream()
                .collect(Collectors.toMap(ProductResponse::getId, p -> p));
    }

    private Map<UUID, ProductResponse> findProducts(List<ProductSelect> selects) {
        List<UUID> productIds = selects.stream()
                .map(ProductSelect::getProductId)
                .toList();
        return findProductsByIds(productIds);
    }

    private BigDecimal calcTotalPrice(Collection<OrderItem> orderItems) {
        var total = new BigDecimal(0);
        for (OrderItem orderItem : orderItems) {
            total = total.add(orderItem.getItemPrice().multiply(new BigDecimal(orderItem.getQuantity())));
        }
        return total;
    }
}
