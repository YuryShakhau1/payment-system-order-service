package by.shakhau.ps.order.messaging.mapper;

import by.shakhau.ps.order.messaging.event.UserUpdatedEvent;
import by.shakhau.ps.order.service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserEventMapper {

    @Mapping(source = "userId", target = "id")
    User toUser(UserUpdatedEvent event);
}
