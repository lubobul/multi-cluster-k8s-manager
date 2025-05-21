package com.multikube_rest_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// import lombok.ToString; // Only if you add a bidirectional mapping to User
// import java.util.Set; // Only if you add a bidirectional mapping to User

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Matches BIGINT AUTO_INCREMENT
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name; // e.g., 'PROVIDER_ADMIN', 'TENANT_USER'

    // If you decide you need to navigate from Role to User (bidirectional):
    // @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    // @ToString.Exclude // Important to prevent stack overflow with toString()
    // private Set<User> users = new HashSet<>();
}