package com.multikube_rest_service.entities.tenant;

import com.multikube_rest_service.common.enums.ResourceStatus;
import com.multikube_rest_service.common.enums.SyncStatus;
import com.multikube_rest_service.entities.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

/**
 * Represents an instance of a deployed workload in a namespace.
 * This entity is self-contained and holds the final YAML content that was applied
 * to the cluster, making it the "source of intent" for this specific instance.
 * It is decoupled from the original template it may have been created from.
 */
@Entity
@Getter
@Setter
@Table(name = "tenant_workloads", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_namespace_id", "k8s_name", "k8s_kind"})
})
public class TenantWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "k8s_name", nullable = false)
    private String k8sName;

    @Column(name = "k8s_kind", nullable = false, length = 100)
    private String k8sKind;

    @Lob
    @Column(name = "yaml_content", nullable = false, columnDefinition = "TEXT")
    private String yamlContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceStatus status;

    @Column(columnDefinition = "TEXT")
    private String statusDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus = SyncStatus.UNKNOWN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_namespace_id", nullable = false)
    private TenantNamespace tenantNamespace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}