import {Injectable} from '@angular/core';
import {UsersApiService} from '../common/rest/api-services/users-api.service';
import {QueryRequest} from '../common/rest/types/requests/query-request';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../common/rest/types/responses/paginated-response';
import {UserResponse} from '../common/rest/types/responses/user-response';
import {buildQueryParams} from '../common/utils/util-functions';

@Injectable({
    providedIn: 'root',
})
export class UsersService {

    constructor(
        private usersApiService: UsersApiService,
    ) {
    }

    getUsers(queryRequest: QueryRequest): Observable<PaginatedResponse<UserResponse>> {
        queryRequest.excludeSelf = true;
        const params = buildQueryParams(queryRequest) as any;
        params.withFriendsInfo = true;
        return this.usersApiService.getUsers(params);
    }

    getUser(userId: number): Observable<UserResponse> {
        return this.usersApiService.getUser(userId);
    }

}
