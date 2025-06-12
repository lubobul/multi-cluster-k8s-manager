import {Component, OnInit} from '@angular/core';
import {ClrAlertModule, ClrDatagridModule, ClrDatagridStateInterface} from '@clr/angular';
import {DatePipe} from '@angular/common';
import {PaginatedResponse} from '../../../common/rest/types/responses/paginated-response';
import {TenantResponse} from '../../../common/rest/types/provider/responses/TenantResponse';
import {QueryRequest, QueryRequestSortType} from '../../../common/rest/types/requests/query-request';
import {debounceTime, mergeMap, Subject} from 'rxjs';
import {TenantService} from '../../services/tenant.service';
import {FormBuilder} from '@angular/forms';
import {buildRestGridFilter, resolveErrorMessage} from '../../../common/utils/util-functions';
import {ClusterService} from '../../services/cluster.service';
import {ClusterResponse} from '../../../common/rest/types/provider/responses/ClusterResponse';
import {ClusterDetailsService} from '../../services/cluster-details.service';

@Component({
  selector: 'app-cluster-scope',
    imports: [
        ClrDatagridModule,
        DatePipe,
        ClrAlertModule,
    ],
  templateUrl: './cluster-scope.component.html',
  styleUrl: './cluster-scope.component.scss'
})
export class ClusterScopeComponent implements OnInit {
    errorMessage = "";
    alertClosed = true;
    loading = true;
    private onDataGridRefresh = new Subject<ClrDatagridStateInterface>();

    tenantsPage: PaginatedResponse<TenantResponse> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<TenantResponse>;

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    cluster: ClusterResponse;
    selectedTenant: TenantResponse;

    publishMessage: string = "";
    publishAlertClosed = true;

    constructor(
        private clusterDetailsService: ClusterDetailsService,
        private clusterService: ClusterService,
        private tenantService: TenantService,
        private fb: FormBuilder,
    ) {
    }

    trackTenantItemById(tenant: TenantResponse) {
        return tenant.id;
    }

    ngOnInit(): void{
        this.loading = true;
        this.subscribeToTenantsGrid();
        this.clusterDetailsService.cluster$.subscribe({
            next: (cluster) => {
                this.cluster = cluster;
                this.loading = false;

            },
            error: (err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertClosed = false;
                this.loading = false;
            }
        });
    }

    public subscribeToTenantsGrid(): void {
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
            })).subscribe({
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

    unpublish(): void {
        this.loading = true;
        this.clusterService.deallocateCluster(this.cluster.id).subscribe({
            next: (response) => {
                this.loading = false;
                this.publishMessage = response.message;
                this.publishAlertClosed = false;
                this.clusterDetailsService.updateClusterDetails();
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
                this.loading = false;

            }
        });
    }

    publish(): void {
        this.loading = true;
        this.clusterService.allocateCluster(this.cluster, {
            tenantId: this.selectedTenant.id,
        }).subscribe({
            next: (response) => {
                this.loading = false;
                this.clusterDetailsService.updateClusterDetails();
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
                this.loading = false;

            }
        });
    }

    public refresh(): void {
        this.tenantService.getTenants(this.restQuery).subscribe((response) => {
            this.tenantsPage = response;
        });
    }
}
