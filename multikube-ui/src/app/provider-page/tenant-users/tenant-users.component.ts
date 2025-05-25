import {Component, OnInit} from '@angular/core';
import {UsersService} from '../../services/users.service';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {UserResponse} from '../../common/rest/types/responses/user-response';
import {ClarityModule, ClrDatagridStateInterface} from '@clr/angular';
import {DatePipe} from '@angular/common';
import {debounceTime, mergeMap, Subject} from 'rxjs';
import {QueryRequest, QueryRequestSortType} from '../../common/rest/types/requests/query-request';
import {buildRestGridFilter, resolveErrorMessage} from '../../common/utils/util-functions';

@Component({
    selector: 'tenant-users',
    imports: [ClarityModule, DatePipe,],
    templateUrl: './tenant-users.component.html',
    standalone: true,
    styleUrl: './tenant-users.component.scss'
})
export class TenantUsersComponent implements OnInit{
    private onDataGridRefresh = new Subject<ClrDatagridStateInterface>();
    errorMessage = "";
    alertClosed = true;
    loading = true;

    usersPage: PaginatedResponse<UserResponse> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<UserResponse>;

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };
    constructor(private usersService: UsersService) {
    }

    ngOnInit(): void {
        this.subscribeToUsersGrid();
    }

    public subscribeToUsersGrid(): void{
        this.onDataGridRefresh.pipe(
            debounceTime(500),
            mergeMap((state) => {
                this.loading = true;
                this.restQuery = {
                    pageSize: state?.page?.size || 5,
                    page: state.page?.current || 1,
                    sort: state.sort ? {
                        sortField: state.sort.by as string,
                        sortType: state.sort.reverse ? QueryRequestSortType.DESC : QueryRequestSortType.ASC
                    } : undefined,
                    filter: buildRestGridFilter(state.filters)
                }
                return this.usersService.getUsers(this.restQuery);
            })).subscribe( {
            next: (response) => {
                this.usersPage = response;
                this.loading = false;

            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
            }
        });
    }

    public refreshByGrid(state: ClrDatagridStateInterface): void {
        this.onDataGridRefresh.next(state);
    }

    public refresh(): void{
        this.usersService.getUsers(this.restQuery).subscribe((response) => {
            this.usersPage = response;
        });
    }
}
