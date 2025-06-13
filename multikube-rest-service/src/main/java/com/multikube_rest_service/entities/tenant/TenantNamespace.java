package com.multikube_rest_service.entities.tenant;

import com.multikube_rest_service.common.enums.NamespaceStatus; // Create this enum
import com.multikube_rest_service.entities.Tenant;
import com.multikube_rest_service.entities.provider.KubernetesCluster;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "tenant_namespaces", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kubernetes_cluster_id", "namespace_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantNamespace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kubernetes_cluster_id", nullable = false)
    private KubernetesCluster kubernetesCluster;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NamespaceStatus status = NamespaceStatus.REQUESTED; // Default status

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}