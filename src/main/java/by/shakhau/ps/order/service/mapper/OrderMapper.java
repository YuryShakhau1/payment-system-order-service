package by.shakhau.ps.order.service.mapper;

import by.shakhau.ps.order.repository.entity.OrderEntity;
import by.shakhau.ps.order.service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "deleted", source = "deleted")
    OrderEntity toEntity(Boolean deleted, Order order);
    Order toModel(OrderEntity order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void update(Order source, @MappingTarget OrderEntity target);
}
