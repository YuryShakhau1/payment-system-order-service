package by.shakhau.ps.order.service.mapper;

import by.shakhau.ps.order.client.dto.PaymentCardDto;
import by.shakhau.ps.order.service.model.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardMapper {

    PaymentCard toModel(PaymentCardDto dto);
}
