package by.shakhau.ps.order.service.mapper;

import by.shakhau.ps.order.repository.entity.OrderItemEntity;
import by.shakhau.ps.order.service.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    OrderItemEntity toEntity(OrderItem order);
    OrderItem toModel(OrderItemEntity order);

    OrderItem toModel(BigDecimal price, OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(OrderItem source, @MappingTarget OrderItemEntity target);
}
