export interface UserResponse {
    id: number,
    username: string,
    email: string,
    createdAt: string,
    tenant: TenantResponse;
}

export interface TenantResponse {
    id: number,
    name: string,
    description: string,
    isActive: boolean,
    createdAt: string,
    updatedAt: string
}


export interface UserChatRightsResponse {
    user: UserResponse;
    chatId: number;
}
