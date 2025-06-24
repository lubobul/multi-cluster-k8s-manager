CREATE TABLE template_catalogs
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- The user-friendly name of the catalog (e.g., 'Default Catalog', 'Frontend Applications').
    name        VARCHAR(255) NOT NULL,

    description TEXT,

    -- Foreign key to the tenant who owns this catalog.
    -- A NULL value could represent a system-wide default catalog seeded by the application.
    tenant_id   BIGINT,

    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_catalog_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE CASCADE,

    -- A tenant cannot have two catalogs with the same name.
    UNIQUE (tenant_id, name)
);

CREATE TABLE workload_templates
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- The user-friendly name of the template (e.g., 'Spring Boot Microservice', 'Redis Cache').
    name                VARCHAR(255) NOT NULL,

    description         TEXT,

    -- The type of the template, for future use (e.g., 'YAML', 'HELM_CHART').
    template_type       VARCHAR(50)  NOT NULL,

    -- The full YAML manifest of the template. This is the core of the template.
    yaml_content        TEXT         NOT NULL,

    -- Foreign key to the catalog this template belongs to.
    template_catalog_id BIGINT       NOT NULL,

    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_template_catalog FOREIGN KEY (template_catalog_id) REFERENCES template_catalogs (id) ON DELETE CASCADE
);

-- Table to store tenant-created workloads (e.g., Deployments, StatefulSets)
CREATE TABLE tenant_workloads
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- The unique name for this specific INSTANCE of the workload in the UI.
    name                VARCHAR(255) NOT NULL,

    -- The actual name of the resource in Kubernetes (parsed from the final YAML).
    k8s_name            VARCHAR(255) NOT NULL,

    -- The kind of the resource (parsed from the final YAML).
    k8s_kind            VARCHAR(100) NOT NULL,

    -- The final, potentially user-edited YAML content is stored here.
    -- This is the "source of intent" for this specific workload instance.
    yaml_content        TEXT         NOT NULL,

    -- The status of this workload record within Multikube (e.g., ACTIVE, FAILED).
    status              VARCHAR(50)  NOT NULL,

    status_details      TEXT,

    -- The synchronization status with the live cluster state.
    sync_status         VARCHAR(50)  NOT NULL DEFAULT 'UNKNOWN',

    -- Foreign key to the namespace this workload is deployed in.
    tenant_namespace_id BIGINT       NOT NULL,

    created_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_workload_namespace FOREIGN KEY (tenant_namespace_id) REFERENCES tenant_namespaces (id) ON DELETE CASCADE,

    -- An instance's k8s_name must be unique for its kind within a namespace.
    CONSTRAINT unique_workload_in_namespace UNIQUE (tenant_namespace_id, k8s_name, k8s_kind)
);