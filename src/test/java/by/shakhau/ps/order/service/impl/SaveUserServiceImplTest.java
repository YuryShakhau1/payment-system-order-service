package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.repository.UserRepository;
import by.shakhau.ps.order.repository.entity.UserEntity;
import by.shakhau.ps.order.service.mapper.UserMapper;
import by.shakhau.ps.order.service.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveUserServiceImplTest {

    @Mock
    private UserMapper mapper;

    @Mock
    private UserRepository repository;

    @InjectMocks
    private SaveUserServiceImpl saveUserService;

    @Test
    void shouldSaveUserEntityWhenUserDtoIsProvided() {
        var user = new User();
        var userEntity = new UserEntity();

        when(mapper.toEntity(user)).thenReturn(userEntity);
        when(repository.save(userEntity)).thenReturn(userEntity);

        saveUserService.save(user);

        verify(mapper, times(1)).toEntity(user);
        verify(repository, times(1)).save(userEntity);
    }

    @Test
    void shouldThrowDataIntegrityViolationExceptionWhenDatabaseCollisionOccurs() {
        var user = new User();
        var userEntity = new UserEntity();

        when(mapper.toEntity(user)).thenReturn(userEntity);
        when(repository.save(userEntity)).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThrows(DataIntegrityViolationException.class, () -> saveUserService.save(user));
        verify(mapper, times(1)).toEntity(user);
        verify(repository, times(1)).save(userEntity);
    }
}
