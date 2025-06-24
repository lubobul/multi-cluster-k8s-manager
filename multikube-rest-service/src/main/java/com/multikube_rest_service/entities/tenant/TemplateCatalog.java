package com.multikube_rest_service.entities.tenant;

import com.multikube_rest_service.entities.Tenant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a catalog for grouping workload templates. A catalog can be owned by a tenant,
 * or it can be a system-wide default catalog (when tenant is null).
 */
@Entity
@Getter
@Setter
@Table(name = "template_catalogs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "name"})
})
public class TemplateCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id") // Nullable to allow for system-wide default catalogs
    private Tenant tenant;

    @OneToMany(mappedBy = "templateCatalog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WorkloadTemplate> workloadTemplates = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}