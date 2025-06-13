package com.multikube_rest_service.entities.tenant;

import com.multikube_rest_service.common.enums.NamespaceStatus;
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "tenant_namespaces", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "cluster_id"})
})
public class TenantNamespace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NamespaceStatus status;

    @Column(columnDefinition = "TEXT")
    private String statusDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cluster_id", nullable = false)
    private KubernetesCluster kubernetesCluster;

    @OneToMany(mappedBy = "tenantNamespace", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TenantNamespaceConfiguration> configurations = new HashSet<>();

    @OneToMany(mappedBy = "tenantNamespace", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TenantWorkload> workloads = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}