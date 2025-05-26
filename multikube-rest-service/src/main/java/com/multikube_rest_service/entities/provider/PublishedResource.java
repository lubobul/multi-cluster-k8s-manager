package com.multikube_rest_service.entities.provider;

import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "published_resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublishedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kubernetes_cluster_id", nullable = false)
    private KubernetesCluster kubernetesCluster;

    @Column(name = "resource_name", nullable = false, unique = true)
    private String resourceName;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType; // Consider an Enum here too: e.g., HELM_CHART, YAML_TEMPLATE

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "details_config", columnDefinition = "TEXT")
    private String detailsConfig; // YAML/JSON template, Helm chart info, etc.

    @Column(name = "is_available_to_all_tenants", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAvailableToAllTenants = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_provider_user_id", nullable = false)
    private User createdByProviderUser;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "published_resource_tenant_access",
            joinColumns = @JoinColumn(name = "published_resource_id"),
            inverseJoinColumns = @JoinColumn(name = "tenant_id")
    )
    private Set<Tenant> accessibleTenants = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}