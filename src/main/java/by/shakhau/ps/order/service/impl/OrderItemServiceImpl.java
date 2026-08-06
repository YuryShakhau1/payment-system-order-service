package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.repository.OrderItemRepository;
import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderItemEntity;
import by.shakhau.ps.order.service.OrderItemService;
import by.shakhau.ps.order.service.exception.ResourceForbiddenException;
import by.shakhau.ps.order.service.exception.ResourceNotFoundException;
import by.shakhau.ps.order.service.mapper.OrderItemMapper;
import by.shakhau.ps.order.service.model.OrderItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemMapper mapper;
    private final OrderItemRepository repository;
    private final OrderRepository orderRepository;

    @Override
    public OrderItem findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException("Order with di = %s not found".formatted(id)));
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).stream()
                .map(mapper::toModel)
                .toList();
    }

    @Transactional
    @Override
    public Collection<OrderItem> create(UUID orderId, Collection<OrderItem> orderItems) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    List<OrderItemEntity> ordersToSave = orderItems.stream()
                            .map(oi -> {
                                OrderItemEntity orderItemEntity = mapper.toEntity(oi);
                                orderItemEntity.setOrder(order);
                                return orderItemEntity;
                            })
                            .toList();
                    return repository.saveAll(ordersToSave);
                })
                .stream()
                .flatMap(Collection::stream)
                .map(mapper::toModel)
                .toList();
    }

    @Transactional
    @Override
    public OrderItem update(OrderItem orderItem) {
        if (orderItem.getId() == null) {
            throw new ResourceForbiddenException("Order ID must not be null");
        }

        OrderItemEntity orderItemEntity = repository.findById(orderItem.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product with id = %s not found".formatted(orderItem.getId())));

        mapper.update(orderItem, orderItemEntity);

        return mapper.toModel(repository.save(orderItemEntity));
    }
}
