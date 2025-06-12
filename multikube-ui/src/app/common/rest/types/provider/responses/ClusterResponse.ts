export interface ClusterResponse {
    id: number,
    name: string,
    description: string,
    providerUserId: number,
    providerUsername: string,
    status: ClusterStatus,
    createdAt: string,
    updatedAt: string
    allocation?: ClusterAllocation;
}

export interface ClusterAllocation {
    tenantName: string;
    tenantId: number
}

export enum ClusterStatus {
    PENDING_VERIFICATION = "PENDING_VERIFICATION", // Initial status upon registration
    ACTIVE = "ACTIVE",               // Verified and reachable
    UNREACHABLE = "UNREACHABLE",          // Verification failed or lost connectivity
    DEGRADED = "DEGRADED",             // Partially reachable or issues detected
    INACTIVE = "INACTIVE",             // Manually set to inactive by provider
    DELETING = "DELETING",             // Marked for deletion
    ERROR = "ERROR"                 // An unspecified error state
}
