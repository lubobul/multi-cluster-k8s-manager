import {TenantResponse} from '../provider/responses/TenantResponse';

export interface UserResponse {
    id: number,
    username: string,
    email: string,
    createdAt: string,
    "roles": UserRoles[],
    tenant: TenantResponse;
}

export enum UserRoles{
    PROVIDER_ADMIN = "PROVIDER_ADMIN",
}
export interface UserChatRightsResponse {
    user: UserResponse;
    chatId: number;
}
