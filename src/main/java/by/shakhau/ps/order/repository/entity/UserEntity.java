package by.shakhau.ps.order.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "cached_users")
@Getter
@Setter
@EqualsAndHashCode(of = {"email"}, callSuper = false)
public class UserEntity {

    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}
