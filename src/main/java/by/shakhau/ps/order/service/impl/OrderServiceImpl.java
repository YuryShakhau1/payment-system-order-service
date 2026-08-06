package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.repository.OrderRepository;
import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderStatus;
import by.shakhau.ps.order.repository.specification.OrderSpecifications;
import by.shakhau.ps.order.service.OrderService;
import by.shakhau.ps.order.service.exception.ResourceForbiddenException;
import by.shakhau.ps.order.service.exception.ResourceNotFoundException;
import by.shakhau.ps.order.service.mapper.OrderMapper;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.OrderItem;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper mapper;
    private final OrderRepository repository;

    @Override
    public Order findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException("Order with di = %s not found".formatted(id)));
    }

    @Override
    public List<Order> findByUserId(UUID userId) {
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

    @Transactional
    @Override
    public Order create(Order order) {
        if (order.getId() != null) {
            throw new ResourceForbiddenException("Order ID must be null");
        }

        return mapper.toModel(repository.save(mapper.toEntity(false, order)));
    }

    @Transactional
    @Override
    public Order update(Order order) {
        if (order.getId() == null) {
            throw new ResourceForbiddenException("Order ID must not be null");
        }

        OrderEntity orderEntity = repository.findById(order.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product with id = %s not found".formatted(order.getId())));

        mapper.update(order, orderEntity);

        return mapper.toModel(repository.save(orderEntity));
    }

    @Transactional
    @Override
    public void updateDeleted(UUID orderId, Boolean deleted) {
        repository.updateDeleted(orderId, deleted);
    }
}
