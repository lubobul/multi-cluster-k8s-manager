export interface TenantResponse {
    id: number;
    name: string;
    description: string;
    isActive: boolean;
    createdAt: string;
    updatedAt: string;
    allocatedClusterIds?: number[];
}
