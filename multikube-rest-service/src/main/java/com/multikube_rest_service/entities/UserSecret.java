package com.multikube_rest_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "user_secrets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSecret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Matches BIGINT AUTO_INCREMENT
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true) // Foreign key to users table, unique enforces 1-to-1
    @ToString.Exclude // Important for bidirectional one-to-one
    private User user;

    @Column(length = 255, nullable = false) // Matches VARCHAR(255) NOT NULL
    private String password; // Store hashed passwords here
}