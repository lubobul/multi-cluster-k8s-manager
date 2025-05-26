package com.multikube_rest_service.common.enums;

public enum ClusterStatus {
    PENDING_VERIFICATION, // Initial status upon registration
    ACTIVE,               // Verified and reachable
    UNREACHABLE,          // Verification failed or lost connectivity
    DEGRADED,             // Partially reachable or issues detected
    INACTIVE,             // Manually set to inactive by provider
    DELETING,             // Marked for deletion
    ERROR                 // An unspecified error state
}