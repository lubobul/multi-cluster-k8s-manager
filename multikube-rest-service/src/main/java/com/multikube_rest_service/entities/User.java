package com.multikube_rest_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString; // Recommended for bidirectional relationships

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Matches BIGINT AUTO_INCREMENT
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "created_at", updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true; // Java default aligns with DB default

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude // Important for bidirectional one-to-one to avoid stack overflow on toString()
    private UserSecret userSecret;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"), // Foreign key to users table
            inverseJoinColumns = @JoinColumn(name = "role_id") // Foreign key to roles table
    )
    private Set<Role> roles = new HashSet<>();

    // Helper method to associate UserSecret (important for bidirectional management)
    public void setUserSecret(UserSecret userSecret) {
        if (userSecret == null) {
            if (this.userSecret != null) {
                this.userSecret.setUser(null);
            }
        } else {
            userSecret.setUser(this);
        }
        this.userSecret = userSecret;
    }
}