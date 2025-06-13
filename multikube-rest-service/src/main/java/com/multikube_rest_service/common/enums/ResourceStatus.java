package com.multikube_rest_service.common.enums;

/**
 * Represents the status of a managed resource record within Multikube.
 */
public enum ResourceStatus {
    /**
     * The resource is being created or applied to the cluster.
     */
    PROCESSING,
    /**
     * The resource is active and believed to be running in the cluster.
     */
    ACTIVE,
    /**
     * The last operation on this resource failed. See status_details for more info.
     */
    ERROR,
    /**
     * The resource is marked for deletion.
     */
    DELETING
}