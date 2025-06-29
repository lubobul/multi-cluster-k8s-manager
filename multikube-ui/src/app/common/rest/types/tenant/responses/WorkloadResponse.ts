/**
 * Represents a lightweight summary of a user.
 * This matches the UserSummaryDto from the backend.
 */
export interface UserSummary {
    id: number;
    username: string;
}

/**
 * Represents the status of a managed resource record within Multikube.
 * This should match the ResourceStatus enum in the backend.
 */
export enum ResourceStatus {
    PROCESSING = 'PROCESSING',
    ACTIVE = 'ACTIVE',
    ERROR = 'ERROR',
    DELETING = 'DELETING',
}

/**
 * Represents the synchronization status of a managed resource.
 * This should match the SyncStatus enum in the backend.
 */
export enum SyncStatus {
    UNKNOWN = 'UNKNOWN',
    IN_SYNC = 'IN_SYNC',
    DRIFT_DETECTED = 'DRIFT_DETECTED',
}

/**
 * Represents a detailed view of a deployed workload instance, including the full final YAML content.
 * This interface matches the TenantWorkloadDto from the backend.
 */
export interface WorkloadResponse {
    /**
     * The unique identifier for the workload instance.
     */
    id: number;

    /**
     * The user-friendly name for this specific workload instance.
     */
    name: string;

    /**
     * The actual name of the resource in Kubernetes (from its metadata.name).
     */
    k8sName: string;

    /**
     * The Kubernetes Kind of the resource (e.g., 'Deployment', 'Service').
     */
    k8sKind: string;

    /**
     * The final YAML manifest that was applied to the cluster for this instance.
     */
    yamlContent?: string;

    /**
     * The user who created this workload instance.
     */
    owner?: UserSummary;

    /**
     * The status of the workload record within Multikube.
     */
    status: ResourceStatus;

    /**
     * The synchronization status of the workload record with the live cluster.
     */
    syncStatus: SyncStatus;

    /**
     * A field containing details about the last operation, especially in case of an error.
     */
    statusDetails: string | null;

    /**
     * The ID of the parent namespace this workload belongs to.
     */
    namespaceId: number;

    /**
     * The timestamp of when the workload was created, as an ISO 8601 string.
     */
    createdAt: string;

    /**
     * The timestamp of the last update to the workload, as an ISO 8601 string.
     */
    updatedAt: string;
}
