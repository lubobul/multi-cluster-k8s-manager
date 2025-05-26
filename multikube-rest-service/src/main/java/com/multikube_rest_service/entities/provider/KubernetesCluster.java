package com.multikube_rest_service.entities.provider;

import com.multikube_rest_service.common.enums.ClusterStatus; // Make sure this enum is created
import com.multikube_rest_service.entities.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "kubernetes_clusters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KubernetesCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "kubeconfig_encrypted", nullable = false, columnDefinition = "TEXT")
    private String kubeconfigEncrypted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_user_id", nullable = false)
    private User providerUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClusterStatus status = ClusterStatus.PENDING_VERIFICATION;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}