package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.repository.UserRepository;
import by.shakhau.ps.order.service.SaveUserService;
import by.shakhau.ps.order.service.mapper.UserMapper;
import by.shakhau.ps.order.service.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveUserServiceImpl implements SaveUserService {

    private final UserMapper mapper;
    private final UserRepository repository;

    @Transactional
    @Override
    public void save(User user) {
        repository.save(mapper.toEntity(user));
    }
}
