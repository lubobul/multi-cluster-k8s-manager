/**
 * Represents the payload for creating a new workload instance.
 * This interface matches the CreateWorkloadRequest DTO from the backend.
 */
export interface CreateWorkloadRequest {
    /**
     * The user-friendly name for this specific workload instance.
     */
    name: string;

    /**
     * An optional description for this instance.
     */
    description?: string | null;

    /**
     * The final, potentially edited YAML manifest to be applied to the cluster.
     */
    yamlContent: string;
}
