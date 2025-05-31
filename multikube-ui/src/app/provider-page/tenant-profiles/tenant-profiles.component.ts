import { Component } from '@angular/core';
import {ClrAlertModule, ClrDatagridModule, ClrDatagridStateInterface, ClrIconModule} from "@clr/angular";
import {DatePipe} from "@angular/common";
import {debounceTime, mergeMap, Subject} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {QueryRequest, QueryRequestSortType} from '../../common/rest/types/requests/query-request';
import {buildRestGridFilter, resolveErrorMessage} from '../../common/utils/util-functions';
import {TenantService} from '../services/tenant.service';
import {TenantResponse} from '../../common/rest/types/provider/responses/TenantResponse';

@Component({
  selector: 'app-tenant-profiles',
    imports: [
        ClrAlertModule,
        ClrDatagridModule,
        DatePipe,
        ClrIconModule
    ],
  templateUrl: './tenant-profiles.component.html',
  styleUrl: './tenant-profiles.component.scss'
})
export class TenantProfilesComponent {

    private onDataGridRefresh = new Subject<ClrDatagridStateInterface>();
    errorMessage = "";
    alertClosed = true;
    loading = true;

    tenantsPage: PaginatedResponse<TenantResponse> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<TenantResponse>;

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };
    constructor(private tenantService: TenantService) {
    }

    ngOnInit(): void {
        this.subscribeToTenantsGrid();
    }

    public subscribeToTenantsGrid(): void{
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
                return this.tenantService.getTenants(this.restQuery);
            })).subscribe( {
            next: (response) => {
                this.tenantsPage = response;
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
        this.tenantService.getTenants(this.restQuery).subscribe((response) => {
            this.tenantsPage = response;
        });
    }
}
