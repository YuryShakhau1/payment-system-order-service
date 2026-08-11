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

}
