/**
* Represents the status of a Kubernetes resource.
*/
export enum ResourceStatus {
    /**
     * The resource is being created or applied to the cluster.
     */
    PROCESSING = 'PROCESSING',

    /**
     * The resource is active and believed to be running in the cluster.
     */
    ACTIVE = 'ACTIVE',

    /**
     * The last operation on this resource failed. See status_details for more info.
     */
    ERROR = 'ERROR',

    /**
     * The resource is marked for deletion.
     */
    DELETING = 'DELETING',
}

/**
 * Represents the synchronization status of a managed resource
 * between the database (source of intent) and the live cluster (source of truth).
 */
export enum SyncStatus {
    /**
     * The state of the resource has not yet been checked against the cluster.
     */
    UNKNOWN = 'UNKNOWN',

    /**
     * The resource in the database matches the resource in the live cluster.
     */
    IN_SYNC = 'IN_SYNC',

    /**
     * The resource in the live cluster has been modified externally and differs from the intended state in the database.
     */
    DRIFT_DETECTED = 'DRIFT_DETECTED',
}

export interface NamespaceWorkloadResponse {
    id: number;
    name: string;
    k8sName: string;
    k8sKind: string;
    status: ResourceStatus;
    syncStatus: SyncStatus;
    statusDetails: string;
    createdAt: string | Date; // Use string for ISO format, Date for JS objects
    updatedAt: string | Date;
}

export interface NamespaceConfigurationResponse {
    id: number;
    name: string;
    k8sName: string;
    k8sKind: string;
    status: ResourceStatus;
    syncStatus: SyncStatus;
    statusDetails: string;
    createdAt: string | Date; // Use string for ISO format, Date for JS objects
    updatedAt: string | Date;
}
