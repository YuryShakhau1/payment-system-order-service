package by.shakhau.ps.order.service.impl;

import by.shakhau.ps.order.client.UserClient;
import by.shakhau.ps.order.repository.UserRepository;
import by.shakhau.ps.order.repository.entity.UserEntity;
import by.shakhau.ps.order.service.SaveUserService;
import by.shakhau.ps.order.service.mapper.UserMapper;
import by.shakhau.ps.order.service.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper mapper;

    @Mock
    private UserClient client;

    @Mock
    private UserRepository repository;

    @Mock
    private SaveUserService saveUserService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldFetchByIdWhenUserExistsInDb() {
        var userId = UUID.randomUUID();
        var userEntity = new UserEntity();
        var expectedUser = new User();

        when(repository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(mapper.toModel(userEntity)).thenReturn(expectedUser);

        User actualUser = userService.fetchById(userId);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
        verifyNoInteractions(client, saveUserService);
        verify(repository, times(1)).findById(userId);
    }

    @Test
    void shouldFetchByIdWhenUserNotInDb() {
        var userId = UUID.randomUUID();
        var expectedUser = new User();

        when(repository.findById(userId)).thenReturn(Optional.empty());
        when(mapper.toModel(null)).thenReturn(null);
        when(client.findUserById(userId)).thenReturn(expectedUser);

        User actualUser = userService.fetchById(userId);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
        verify(client, times(1)).findUserById(userId);
        verify(saveUserService, times(1)).save(expectedUser);
    }

    @Test
    void shouldFetchByIdWhenCollisionOccurs() {
        var userId = UUID.randomUUID();
        var externalUser = new User();
        var parallelSavedEntity = new UserEntity();
        var expectedUser = new User();

        when(repository.findById(userId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(parallelSavedEntity));

        when(mapper.toModel(null)).thenReturn(null);
        when(client.findUserById(userId)).thenReturn(externalUser);

        doThrow(new DataIntegrityViolationException("Duplicate key"))
                .when(saveUserService).save(externalUser);

        when(mapper.toModel(parallelSavedEntity)).thenReturn(expectedUser);

        User actualUser = userService.fetchById(userId);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
        verify(client, times(1)).findUserById(userId);
        verify(saveUserService, times(1)).save(externalUser);
        verify(repository, times(2)).findById(userId);
    }
}
