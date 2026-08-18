package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.ProductClient;
import by.shakhau.ps.order.client.dto.Product;
import by.shakhau.ps.order.client.dto.ProductIndices;
import by.shakhau.ps.order.exception.OperationForbiddenException;
import by.shakhau.ps.order.exception.ResourceNotFoundException;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.repository.specification.OrderSpecifications;
import by.shakhau.ps.order.service.OrderService;
import by.shakhau.ps.order.service.ProductSnapshotService;
import by.shakhau.ps.order.service.UserService;
import by.shakhau.ps.order.service.mapper.OrderMapper;
import by.shakhau.ps.order.service.model.Actor;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.OrderItem;
import by.shakhau.ps.order.service.model.OrderItemUpdate;
import by.shakhau.ps.order.service.model.ProductSelect;
import by.shakhau.ps.order.service.model.ProductSnapshot;
import by.shakhau.ps.order.service.model.UpdateItem;
import by.shakhau.ps.order.service.model.UpdateOrder;
import by.shakhau.ps.order.service.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static by.shakhau.ps.order.repository.entity.OrderStatus.CANCELLED;
import static by.shakhau.ps.order.repository.entity.OrderStatus.COMPLETED;
import static by.shakhau.ps.order.repository.entity.OrderStatus.CREATED;
import static by.shakhau.ps.order.repository.entity.OrderStatus.DELETED;
import static by.shakhau.ps.order.repository.entity.OrderStatus.IN_DELIVERY;
import static by.shakhau.ps.order.repository.entity.OrderStatus.PAID;
import static by.shakhau.ps.order.repository.entity.OrderStatus.PAYMENT_FAILED;
import static by.shakhau.ps.order.repository.entity.OrderStatus.PAYMENT_IN_PROCESS;
import static by.shakhau.ps.order.repository.entity.OrderStatus.REFUNDED;
import static by.shakhau.ps.order.repository.entity.OrderStatus.REFUND_IN_PROCESS;
import static by.shakhau.ps.order.service.model.Actor.ADMIN;
import static by.shakhau.ps.order.service.model.Actor.SYSTEM;
import static by.shakhau.ps.order.service.model.Actor.USER;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper mapper;
    private final OrderRepository repository;
    private final ProductClient productClient;
    private final UserService userService;
    private final ProductSnapshotService productSnapshotService;

    private static final Map<Actor, Map<OrderStatus, Set<OrderStatus>>> AVAILABLE_STATUS_CHANGES = new HashMap<>();

    static {
        AVAILABLE_STATUS_CHANGES.put(SYSTEM, Map.of(
                CREATED, Set.of(PAYMENT_IN_PROCESS),
                PAYMENT_FAILED, Set.of(PAYMENT_IN_PROCESS),
                PAYMENT_IN_PROCESS, Set.of(PAID, PAYMENT_FAILED),
                REFUND_IN_PROCESS, Set.of(REFUNDED, PAID)
        ));
        AVAILABLE_STATUS_CHANGES.put(ADMIN, Map.of(
                CREATED, Set.of(CANCELLED),
                PAYMENT_FAILED, Set.of(CANCELLED),
                CANCELLED, Set.of(DELETED, CREATED),
                DELETED, Set.of(CANCELLED),
                PAID, Set.of(IN_DELIVERY, REFUND_IN_PROCESS),
                IN_DELIVERY, Set.of(COMPLETED)
        ));
        AVAILABLE_STATUS_CHANGES.put(USER, Map.of(
                CREATED, Set.of(CANCELLED),
                PAYMENT_FAILED, Set.of(CANCELLED),
                CANCELLED, Set.of(DELETED, CREATED),
                PAID, Set.of(REFUND_IN_PROCESS)
        ));
    }

    @Override
    public Order findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toModel)
                .map(this::fillUser)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id = %s not found".formatted(id)));
    }

    @Override
    public Order findByIdAndUserId(UUID id, UUID userId) {
        return repository.findByIdAndUserId(id, userId)
                .map(mapper::toModel)
                .map(this::fillUser)
                .orElseThrow(() -> new ResourceNotFoundException("Order with id = %s not found".formatted(id)));
    }

    @Override
    public List<Order> findByUserId(UUID userId, boolean withItems) {
        Map<UUID, User> userCache = new HashMap<>();
        if (!withItems) {
            return repository.findByUserIdWithoutItems(userId).stream()
                    .map(mapper::toModelWithoutItems)
                    .map(o -> fillUser(o, userCache))
                    .toList();
        }

        return repository.findByUserId(userId).stream()
                .map(mapper::toModel)
                .map(o -> fillUser(o, userCache))
                .toList();
    }

    @Override
    public Page<Order> findFiltered(
            UUID userId,
            LocalDateTime from, LocalDateTime to,
            Collection<OrderStatus> statuses,
            Boolean deleted,
            Pageable pageable) {
        List<Specification<OrderEntity>> specs = new ArrayList<>();

        if (userId != null) {
            specs.add(OrderSpecifications.withUserId(userId));
        }
        if (from != null) {
            specs.add(OrderSpecifications.createdAfter(from));
        }
        if (to != null) {
            specs.add(OrderSpecifications.createdBefore(to));
        }
        if (statuses != null && !statuses.isEmpty()) {
            specs.add(OrderSpecifications.hasStatuses(statuses));
        }
        if (deleted != null) {
            specs.add(OrderSpecifications.deleted(deleted));
        }

        Map<UUID, User> userCache = new HashMap<>();
        Specification<OrderEntity> finalSpecification = Specification.allOf(specs);
        return repository.findAll(finalSpecification, pageable)
                .map(mapper::toModel)
                .map(o -> fillUser(o, userCache));
    }

    @Transactional
    @Override
    public Order create(UUID userId, List<ProductSelect> selects) {
        List<ProductSelect> mergedSelects = mergeSelectsByProducts(selects);
        Map<UUID, Product> productMap = findProducts(mergedSelects);
        List<OrderItem> items = createOrderItems(selects, productMap);

        Map<UUID, ProductSnapshot> products = productSnapshotService.fetchByProductIds(
                selects.stream().map(ProductSelect::getProductId).toList());

        items.forEach(item -> item.setProductSnapshot(products.get(item.getProductId())));

        var order = Order.builder()
                .userId(userId)
                .status(OrderStatus.getBeginStatus())
                .totalPrice(calcTotalPrice(items))
                .items(items)
                .deleted(false)
                .build();

        order = mapper.toModel(repository.save(mapper.toEntity(order)));
        return fillUser(order);
    }

    @Transactional
    @Override
    public Order update(UUID userId, UUID id, UpdateOrder updateOrder) {
        Order order = findByIdAndUserId(id, userId);
        if (order.getStatus() != CREATED && order.getStatus() != PAYMENT_FAILED) {
            return order;
        }

        Map<UUID, UpdateItem> updateItems = updateOrder.getUpdateItems().stream()
                .collect(Collectors.toMap(UpdateItem::getId, i -> i));

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

        Map<UUID, Product> productMap = findProductsByIds(productIds);

        createItems.addAll(orderItemUpdates.stream()
                .map(update -> applyUpdates(productMap, update))
                .filter(Objects::nonNull)
                .toList());

        itemsToSave.addAll(createOrderItems(createItems, productMap));

        order.setItems(itemsToSave);
        order.setTotalPrice(calcTotalPrice(order.getItems()));

        return fillUser(mapper.toModel(repository.save(mapper.toEntity(order))));
    }

    @Transactional
    @Override
    public Order updateStatus(Order order, OrderStatus status, Actor actor) {
        validateOrderStatusChange(actor, order, status);

        OrderStatus orderStatus = order.getStatus();
        if (repository.updateStatus(order.getId(), orderStatus.getValue(), status.getValue()) != 1) {
            throw new OperationForbiddenException(
                    "Current order status %s changed during operation.".formatted(order.getStatus()));
        }

        if (status == DELETED) {
            repository.updateDeleted(order.getId(), true);
        } else if (status == CANCELLED && orderStatus == DELETED) {
            repository.updateDeleted(order.getId(), false);
        }

        order.setStatus(status);
        return fillUser(order);
    }

    @Transactional
    @Override
    public Order updateStatus(UUID userId, UUID id, OrderStatus status, Actor actor) {
        Order order = findByIdAndUserId(id, userId);
        return updateStatus(order, status, actor);
    }

    private Order fillUser(Order order) {
        order.setUser(userService.fetchById(order.getUserId()));
        return order;
    }

    private Order fillUser(Order order, Map<UUID, User> userCache) {
        User user = userCache.computeIfAbsent(
                order.getUserId(), k -> userService.fetchById(order.getUserId()));
        order.setUser(user);
        return order;
    }

    private void validateOrderStatusChange(Actor actor, Order order, OrderStatus status) {
        boolean statusChangeAvailable = AVAILABLE_STATUS_CHANGES
                .getOrDefault(actor, Collections.emptyMap())
                .getOrDefault(order.getStatus(), Collections.emptySet())
                .contains(status);
        if (!statusChangeAvailable) {
            throw new OperationForbiddenException(
                    "Order status %s can not be changed to %s".formatted(order.getStatus(), status));
        }
    }

    private ProductSelect applyUpdates(Map<UUID, Product> productMap, OrderItemUpdate orderItemUpdate) {
        OrderItem item = orderItemUpdate.getItem();
        UpdateItem updateItem = orderItemUpdate.getUpdateItem();
        if (updateItem.getQuantity() > item.getQuantity()) {
            Product product = productMap.get(item.getProductId());
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
            List<ProductSelect> selects, Map<UUID, Product> productMap) {
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

    private Map<UUID, Product> findProductsByIds(Collection<UUID> productIds) {
        var productsRequest = new ProductIndices(productIds);
        return productClient.findProducts(productsRequest).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private Map<UUID, Product> findProducts(List<ProductSelect> selects) {
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
