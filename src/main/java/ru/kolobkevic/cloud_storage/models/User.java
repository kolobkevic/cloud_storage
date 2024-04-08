package ru.kolobkevic.cloud_storage.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Поле не должно быть пустым")
    @Email
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Size(min = 2, max = 20, message = "Длина имени должна быть больше 2 и меньше 20 букв")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Size(min = 2, max = 30, message = "Длина фамилии должна быть больше 2 и меньше 30 букв")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Size(min = 6, max = 20, message = "Длина пароля должна быть больше 6 и меньше 20 символов")
    @Column(name = "password")
    private String password;

    @Column(name = "created_at")
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;
}
