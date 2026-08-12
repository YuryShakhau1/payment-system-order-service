package by.shakhau.ps.order.controller.dto.mapper;

import by.shakhau.ps.order.controller.dto.request.PaymentCardRequest;
import by.shakhau.ps.order.service.model.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardDtoMapper {

    PaymentCard toModel(PaymentCardRequest request);
}
