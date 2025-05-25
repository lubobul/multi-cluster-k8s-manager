import {UserResponse} from './responses/user-response';

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface JwtResponse {
    user: UserResponse;
    token: string;
}

export interface RestMessageResponse {
    message: string;
}

export interface UpdateProfileRequest {
    username: string;
    avatar: string;
}
