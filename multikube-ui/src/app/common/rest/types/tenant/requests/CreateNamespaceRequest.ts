// create-namespace-request.ts

export interface CreateNamespaceRequest {
    /**
     * The name for the new namespace.
     * - Cannot be blank.
     * - Must be a DNS-compliant name (e.g., 'my-awesome-namespace').
     * - Pattern: /^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/
     */
    name: string;

    /**
     * An optional description for the namespace.
     */
    description?: string;

    /**
     * The ID of the target cluster where the namespace will be created.
     * - A target cluster ID must be provided.
     */
    clusterId: number;

    /**
     * Optional: A string containing the YAML manifest for a ResourceQuota object.
     */
    resourceQuotaYaml?: string;

    /**
     * Optional: A string containing the YAML manifest for a LimitRange object.
     */
    limitRangeYaml?: string;
}
