package by.shakhau.ps.order.service;

import by.shakhau.ps.order.service.model.User;

import java.util.UUID;

public interface UserService {

    User fetchById(UUID id);
}
