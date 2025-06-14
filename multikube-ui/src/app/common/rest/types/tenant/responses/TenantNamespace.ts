import {NamespaceConfigurationResponse} from './TenantNamespaceResources';

export enum NamespaceStatus {
    REQUESTED = 'REQUESTED',
    CREATING = 'CREATING',
    ACTIVE = 'ACTIVE',
    DELETING = 'DELETING',
    DELETED = 'DELETED',
    FAILED_CREATION = 'FAILED_CREATION',
    FAILED_DELETION = 'FAILED_DELETION',
    UNKNOWN = 'UNKNOWN'
}

export interface TenantNamespaceResponse {
    id: number;
    name: string;
    description: string;
    status: NamespaceStatus;
    statusDetails: string;
    clusterId: number;
    clusterName: string;
    configurationsCount: number;
    workloadsCount: number;
    createdAt: string | Date; // Use 'string' for ISO 8601 format, 'Date' if you plan to parse it
    updatedAt: string | Date;
}

export interface TenantNamespaceSummaryResponse {
    id: number;
    name: string;
    status: NamespaceStatus;
    clusterId: number;
    clusterName: string;
}
