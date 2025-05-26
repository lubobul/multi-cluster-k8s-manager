-- Table to store registered Kubernetes clusters
CREATE TABLE kubernetes_clusters
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                 VARCHAR(255) NOT NULL UNIQUE,
    description          TEXT,
    kubeconfig_encrypted TEXT         NOT NULL,
    provider_user_id     BIGINT       NOT NULL,                      -- The user (provider admin) who registered this cluster
    status               VARCHAR(50) DEFAULT 'PENDING_VERIFICATION', -- e.g., PENDING_VERIFICATION, ACTIVE, UNREACHABLE, INACTIVE. Managed by Java Enum.
    created_at           TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cluster_provider_user FOREIGN KEY (provider_user_id) REFERENCES users (id) ON DELETE RESTRICT
);

-- Table to associate tenants with specific namespaces on specific Kubernetes clusters
CREATE TABLE tenant_namespaces
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    kubernetes_cluster_id BIGINT       NOT NULL,
    tenant_id             BIGINT       NOT NULL,           -- Foreign key to the 'tenants' table
    namespace_name        VARCHAR(255) NOT NULL,
    status                VARCHAR(50) DEFAULT 'REQUESTED', -- e.g., REQUESTED, CREATING, ACTIVE, DELETING, DELETED, FAILED_CREATION. Managed by Java Enum.
    created_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tn_kubernetes_cluster FOREIGN KEY (kubernetes_cluster_id) REFERENCES kubernetes_clusters (id) ON DELETE CASCADE,
    CONSTRAINT fk_tn_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,
    UNIQUE (kubernetes_cluster_id, namespace_name)
);

-- Table for resources published by the provider
CREATE TABLE published_resources
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    kubernetes_cluster_id       BIGINT       NOT NULL,
    resource_name               VARCHAR(255) NOT NULL UNIQUE,
    resource_type               VARCHAR(100) NOT NULL, -- e.g., "HelmChart", "DeploymentYAML". Managed by Java Enum/constants.
    description                 TEXT,
    details_config              TEXT,                  -- YAML/JSON template, Helm chart info, etc.
    is_available_to_all_tenants BOOLEAN   DEFAULT FALSE,
    created_by_provider_user_id BIGINT       NOT NULL,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pr_kubernetes_cluster FOREIGN KEY (kubernetes_cluster_id) REFERENCES kubernetes_clusters (id) ON DELETE CASCADE,
    CONSTRAINT fk_pr_provider_user FOREIGN KEY (created_by_provider_user_id) REFERENCES users (id) ON DELETE RESTRICT
);

-- Junction table for specific tenant access to published_resources
CREATE TABLE published_resource_tenant_access
(
    published_resource_id BIGINT NOT NULL,
    tenant_id             BIGINT NOT NULL, -- Foreign key to the 'tenants' table
    PRIMARY KEY (published_resource_id, tenant_id),
    CONSTRAINT fk_prta_published_resource FOREIGN KEY (published_resource_id) REFERENCES published_resources (id) ON DELETE CASCADE,
    CONSTRAINT fk_prta_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE
);