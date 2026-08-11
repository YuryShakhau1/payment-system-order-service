package by.shakhau.ps.order.service.mapper;

import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.repository.entity.OrderItemEntity;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "deleted", source = "deleted")
    @Mapping(target = "createdAt", source = "order.createdAt", ignore = true)
    @Mapping(target = "updatedAt", source = "order.updatedAt", ignore = true)
    OrderEntity toEntity(Boolean deleted, Order order);
    Order toModel(OrderEntity order);

    @Mapping(target = "items", source = "items", ignore = true)
    Order toModelWithoutItems(OrderEntity order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(Order source, @MappingTarget OrderEntity target);

    OrderItemEntity mapOrderItemToOrderItemEntity(OrderItem orderItem);

    @Mapping(target = "orderId", source = "order.id")
    OrderItem mapOrderItemEntityToOrderItem(OrderItemEntity orderItemEntity);

    @AfterMapping
    default void setOrderReference(@MappingTarget OrderEntity orderEntity) {
        if (orderEntity.getItems() != null) {
            orderEntity.getItems().forEach(item -> item.setOrder(orderEntity));
        }
    }
}
