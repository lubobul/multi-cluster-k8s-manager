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
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    kubernetes_cluster_id BIGINT       NOT NULL,           -- The cluster this namespace belongs to. Must be a cluster allocated to the tenant.
    tenant_id             BIGINT       NOT NULL,           -- The tenant who created and owns this namespace.
    namespace_name        VARCHAR(255) NOT NULL,
    status                VARCHAR(50) DEFAULT 'REQUESTED', -- Status from the tenant's perspective (e.g., CREATING, ACTIVE, FAILED).
    created_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tn_kubernetes_cluster FOREIGN KEY (kubernetes_cluster_id) REFERENCES kubernetes_clusters (id) ON DELETE CASCADE,
    CONSTRAINT fk_tn_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    UNIQUE (kubernetes_cluster_id, namespace_name)         -- A namespace name is still unique within a given cluster.
);

-- Table to store tenant-created workloads (e.g., Deployments, StatefulSets)
CREATE TABLE tenant_workloads
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,           -- The name of the Kubernetes resource (e.g., 'my-app-deployment').
    workload_type       VARCHAR(50)  NOT NULL,           -- The type of workload (e.g., 'DEPLOYMENT', 'STATEFULSET', 'SERVICE').
    tenant_namespace_id BIGINT       NOT NULL,           -- Foreign key to the namespace this workload belongs to.
    definition          TEXT         NOT NULL,           -- The YAML or JSON manifest provided by the tenant (the "intended state").
    status              VARCHAR(50) DEFAULT 'REQUESTED', -- The status from Multikube's perspective (e.g., 'CREATING', 'ACTIVE', 'FAILED').
    sync_status         VARCHAR(50) DEFAULT 'UNKNOWN',   -- The status of synchronization with the live cluster state (e.g., 'IN_SYNC', 'DRIFT_DETECTED').
    created_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key to link this workload to its parent namespace.
    -- If a namespace is deleted from our system, all its associated workload records should also be deleted.
    CONSTRAINT fk_workload_tenant_namespace FOREIGN KEY (tenant_namespace_id) REFERENCES tenant_namespaces (id) ON DELETE CASCADE,

    -- A workload's name must be unique within its namespace and for its type.
    -- e.g., you can't have two Deployments named 'my-app' in the same namespace,
    -- but you could have a Deployment and a Service named 'my-app'.
    UNIQUE (tenant_namespace_id, name, workload_type)
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