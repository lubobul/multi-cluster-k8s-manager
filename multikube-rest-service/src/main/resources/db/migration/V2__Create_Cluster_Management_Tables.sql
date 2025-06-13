-- Table to store registered Kubernetes clusters (Provider's view of all clusters).
CREATE TABLE kubernetes_clusters
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL UNIQUE,
    description          TEXT,
    kubeconfig_encrypted TEXT         NOT NULL,
    provider_user_id     BIGINT       NOT NULL,
    status               VARCHAR(50) DEFAULT 'PENDING_VERIFICATION', -- e.g., PENDING_VERIFICATION, ACTIVE, UNREACHABLE
    created_at           TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cluster_provider_user FOREIGN KEY (provider_user_id) REFERENCES users (id) ON DELETE RESTRICT
);

-- Represents the allocation of a single Kubernetes cluster to a single tenant.
CREATE TABLE cluster_allocations
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    kubernetes_cluster_id BIGINT NOT NULL UNIQUE,                                                                                  -- UNIQUE constraint ensures a cluster is allocated to only ONE tenant.
    tenant_id             BIGINT NOT NULL,                                                                                         -- The tenant who "owns" this cluster.
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ca_kubernetes_cluster FOREIGN KEY (kubernetes_cluster_id) REFERENCES kubernetes_clusters (id) ON DELETE CASCADE, -- If the cluster registration is deleted, the allocation is removed.
    CONSTRAINT fk_ca_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE                                      -- If the tenant is deleted, their cluster allocations are removed.
);

-- Tracks namespaces created BY A TENANT ADMIN within their own allocated clusters.
CREATE TABLE tenant_namespaces
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- The DNS-compliant name of the namespace in Kubernetes.
    name           VARCHAR(255) NOT NULL,
    -- A user-friendly description for the namespace.
    description    TEXT,
    -- The status of the namespace object itself (e.g., ACTIVE, DELETING, FAILED).
    status         VARCHAR(50)  NOT NULL,
    -- To store any errors related to the creation or deletion of the namespace object itself.
    status_details TEXT,
    -- Foreign keys to the tenant and cluster this namespace belongs to.
    tenant_id      BIGINT       NOT NULL,
    cluster_id     BIGINT       NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- A namespace name must be unique within a given cluster.
    CONSTRAINT unique_namespace_per_cluster UNIQUE (name, cluster_id),

    FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    FOREIGN KEY (cluster_id) REFERENCES kubernetes_clusters (id)
);

-- Tracks resources such as  'ResourceQuota', 'LimitRange', 'NetworkPolicy' etc.,  created BY A TENANT ADMIN within their own allocated clusters.
CREATE TABLE tenant_namespace_configurations
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- A user-friendly name for this configuration in the UI (e.g., "Default Deny Ingress").
    name                VARCHAR(255) NOT NULL,
    -- The actual name of the resource in Kubernetes (from its metadata.name).
    k8s_name            VARCHAR(255) NOT NULL,
    -- The Kubernetes Kind of the resource (e.g., 'ResourceQuota', 'LimitRange', 'NetworkPolicy', 'Role', 'RoleBinding').
    k8s_kind            VARCHAR(100) NOT NULL,
    -- The full YAML content, serving as the "source of intent".
    yaml_content        TEXT         NOT NULL,
    -- The status of this configuration record within Multikube (e.g., ACTIVE, ERROR, DELETING).
    status              VARCHAR(50)  NOT NULL,
    -- To store any error messages from the last apply/delete operation.
    status_details      TEXT,
    -- The status of synchronization with the live cluster state, essential for drift detection.
    sync_status         VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN', -- e.g., 'IN_SYNC', 'DRIFT_DETECTED', 'UNKNOWN'
    -- Foreign key linking back to the parent namespace.
    tenant_namespace_id BIGINT       NOT NULL,
    created_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key to link this configuration to its parent namespace. On deleting the namespace, these records are also deleted.
    CONSTRAINT fk_config_namespace FOREIGN KEY (tenant_namespace_id) REFERENCES tenant_namespaces (id) ON DELETE CASCADE,

    -- A resource's k8s_name must be unique for its kind within a namespace.
    CONSTRAINT unique_config_in_namespace UNIQUE (tenant_namespace_id, k8s_name, k8s_kind)
);

-- Table to store tenant-created workloads (e.g., Deployments, StatefulSets)
CREATE TABLE tenant_workloads
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- A user-friendly name for this workload in the UI (e.g., "My Web Application").
    name                VARCHAR(255) NOT NULL,
    -- The actual name of the resource in Kubernetes (from its metadata.name).
    k8s_name            VARCHAR(255) NOT NULL,
    -- The Kubernetes Kind of the resource (e.g., 'Deployment', 'StatefulSet', 'Service', 'Ingress').
    k8s_kind            VARCHAR(100) NOT NULL,
    -- The full YAML content, serving as the "source of intent".
    yaml_content        TEXT         NOT NULL,
    -- The status of this workload record within Multikube (e.g., ACTIVE, ERROR, DELETING).
    status              VARCHAR(50)  NOT NULL,
    -- To store any error messages from the last apply/delete operation.
    status_details      TEXT,
    -- The status of synchronization with the live cluster state, essential for drift detection.
    sync_status         VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN', -- e.g., 'IN_SYNC', 'DRIFT_DETECTED', 'UNKNOWN'
    -- Foreign key linking back to the parent namespace.
    tenant_namespace_id BIGINT       NOT NULL,
    created_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key to link this workload to its parent namespace. On deleting the namespace, these records are also deleted.
    CONSTRAINT fk_workload_namespace FOREIGN KEY (tenant_namespace_id) REFERENCES tenant_namespaces (id) ON DELETE CASCADE,

    -- A resource's k8s_name must be unique for its kind within a namespace.
    CONSTRAINT unique_workload_in_namespace UNIQUE (tenant_namespace_id, k8s_name, k8s_kind)
);

-- RETAINED TABLE: Holds templates for resources (like Helm charts, standard deployments) that a provider
-- can "publish" or make available for tenants to use within their own clusters and namespaces.
CREATE TABLE published_resources
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_name               VARCHAR(255) NOT NULL UNIQUE,
    resource_type               VARCHAR(100) NOT NULL, -- e.g., "HELM_CHART", "DEPLOYMENT_TEMPLATE".
    description                 TEXT,
    details_config              TEXT,                  -- YAML/JSON template, Helm chart info, etc.
    is_available_to_all_tenants BOOLEAN   DEFAULT FALSE,
    created_by_provider_user_id BIGINT       NOT NULL,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- Note: kubernetes_cluster_id might be removed or repurposed here. A published resource might not be tied
    -- to a specific source cluster anymore, but could be deployable to any cluster. For now, we assume it might
    -- indicate a preferred or tested cluster. Let's keep it for now but acknowledge its role may change.
    kubernetes_cluster_id       BIGINT,
    CONSTRAINT fk_pr_provider_user FOREIGN KEY (created_by_provider_user_id) REFERENCES users (id) ON DELETE RESTRICT
);

-- RETAINED TABLE: Junction table for specific tenant access to published_resources.
-- This allows a provider to publish a resource template only to specific tenants.
CREATE TABLE published_resource_tenant_access
(
    published_resource_id BIGINT NOT NULL,
    tenant_id             BIGINT NOT NULL,
    PRIMARY KEY (published_resource_id, tenant_id),
    CONSTRAINT fk_prta_published_resource FOREIGN KEY (published_resource_id) REFERENCES published_resources (id) ON DELETE CASCADE,
    CONSTRAINT fk_prta_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);