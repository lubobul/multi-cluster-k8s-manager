package com.multikube_rest_service.common.enums;

/**
 * Represents the synchronization status of a managed resource
 * between the database (source of intent) and the live cluster (source of truth).
 */
public enum SyncStatus {
    /**
     * The state of the resource has not yet been checked against the cluster.
     */
    UNKNOWN,
    /**
     * The resource in the database matches the resource in the live cluster.
     */
    IN_SYNC,
    /**
     * The resource in the live cluster has been modified externally and differs from the intended state in the database.
     */
    DRIFT_DETECTED
}