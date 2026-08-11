package by.shakhau.ps.order.controller.dto.mapper;

import by.shakhau.ps.order.controller.dto.request.UpdateOrderRequest;
import by.shakhau.ps.order.controller.dto.response.OrderResponse;
import by.shakhau.ps.order.service.model.Order;
import by.shakhau.ps.order.service.model.UpdateOrder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderDtoMapper {

    OrderResponse toDto(Order order);
    UpdateOrder toModel(UpdateOrderRequest request);
}
