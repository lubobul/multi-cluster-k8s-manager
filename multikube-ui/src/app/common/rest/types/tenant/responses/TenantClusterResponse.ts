import {ClusterStatus} from '../../provider/responses/ClusterResponse';

export interface TenantClusterResponse {
    id: number,
    name: string,
    description: string,
    providerUserId: number,
    status: ClusterStatus,
    createdAt: string,
    updatedAt: string
}

