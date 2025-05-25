export interface UserResponse {
    id: number;
    username: string;
    email: string;
    createdAt: string;
    avatar?: string;
    isFriendOfYours?: boolean;
    chatUserType?: ChatUserType;
}

export enum ChatUserType {
    OWNER = "OWNER",
    ADMIN = "ADMIN",
    PARTICIPANT = "PARTICIPANT"
}

export interface UserChatRightsResponse {
    user: UserResponse;
    chatId: number;
    chatUserType: ChatUserType;
}
