package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.UserClient;
import by.shakhau.ps.order.repository.UserRepository;
import by.shakhau.ps.order.service.SaveUserService;
import by.shakhau.ps.order.service.UserService;
import by.shakhau.ps.order.service.mapper.UserMapper;
import by.shakhau.ps.order.service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper mapper;
    private final UserClient client;
    private final UserRepository repository;
    private final SaveUserService saveUserService;

    @Override
    public User fetchById(UUID id) {
        User foundUser = findById(id);
        if (foundUser == null) {
            foundUser = client.findUserById(id);
            if (foundUser != null) {
                try {
                    saveUserService.save(foundUser);
                } catch (DataIntegrityViolationException e) {
                    foundUser = findById(id);
                }
            }
        }
        return foundUser;
    }

    private User findById(UUID id) {
        return mapper.toModel(repository.findById(id).orElse(null));
    }
}
