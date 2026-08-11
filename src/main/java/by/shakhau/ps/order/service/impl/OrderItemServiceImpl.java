package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.repository.OrderItemRepository;
import by.shakhau.ps.order.service.OrderItemService;
import by.shakhau.ps.order.service.mapper.OrderItemMapper;
import by.shakhau.ps.order.service.model.OrderItem;
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

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public void save(Collection<OrderItem> items) {
        repository.saveAll(items.stream().map(mapper::toEntity).toList());
    }

    @Override
    public void deleteByIds(Collection<UUID> ids) {
        ids.forEach(repository::deleteById);
    }
}
