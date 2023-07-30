package ru.kolobkevic.cloud_storage.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kolobkevic.cloud_storage.entities.enums.RoleType;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false, unique = true)
    private String name;

    @Column(length = 150, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", length = 10, nullable = false)
    private RoleType roleType;

    @Override
    public String toString() {
        return "{ " + name + "/" + roleType + " }";
    }
}
