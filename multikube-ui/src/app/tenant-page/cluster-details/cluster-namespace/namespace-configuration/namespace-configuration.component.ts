import {Component, OnInit} from '@angular/core';
import {TenantNamespaceService} from '../../../services/tenant-namespace.service';
import {ClarityModule, ClrAlertModule, ClrDatagridModule, ClrDatagridStateInterface} from '@clr/angular';
import {DatePipe} from '@angular/common';
import {BehaviorSubject, debounceTime, filter, mergeMap, Subject} from 'rxjs';
import {QueryRequest, QueryRequestSortType} from '../../../../common/rest/types/requests/query-request';
import {buildRestGridFilter, resolveErrorMessage} from '../../../../common/utils/util-functions';
import {PaginatedResponse} from '../../../../common/rest/types/responses/paginated-response';
import {NamespaceConfigurationResponse} from '../../../../common/rest/types/tenant/responses/TenantNamespaceResources';
import {TenantNamespaceResponse} from '../../../../common/rest/types/tenant/responses/TenantNamespace';
import {TenantNamespaceDetailsService} from '../../../services/tenant-namespace-details.service';
import {k8sResourceStatusComponent} from '../../../../common/k8s-resource-status/k8s-resource-status.component';
import {k8sResourceSyncStatusComponent} from '../../../../common/k8s-sync-status/k8s-resource-sync-status.component';
import {EditorComponent} from 'ngx-monaco-editor-v2';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@Component({
    selector: 'app-namespace-configuration',
    imports: [
        ClrAlertModule,
        ClrDatagridModule,
        DatePipe,
        k8sResourceStatusComponent,
        k8sResourceSyncStatusComponent,
        ClarityModule,
        EditorComponent,
        ReactiveFormsModule,
        FormsModule
    ],
    templateUrl: './namespace-configuration.component.html',
    styleUrl: './namespace-configuration.component.scss'
})
export class NamespaceConfigurationComponent implements OnInit {
    private onDataGridRefresh = new BehaviorSubject<ClrDatagridStateInterface>(null);
    errorMessage = "";
    alertErrorClosed = true;
    loading = true;
    namespace: TenantNamespaceResponse;
    configurationsPage: PaginatedResponse<NamespaceConfigurationResponse> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<NamespaceConfigurationResponse>;

    gridNamespaceConfigDetailState: NamespaceConfigurationResponse;
    public editorOptions = {
        theme: "vs",
        language: "yaml",
        automaticLayout: true,
        readOnly: true,
        minimap: {enabled: false}
    };

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    constructor(
        private tenantNamespaceService: TenantNamespaceService,
        private namespaceDetailsService: TenantNamespaceDetailsService,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.subscribeToConfigurationsGrid();
        this.namespaceDetailsService.namespace$.subscribe({
            next: (namespace) => {
                this.namespace = namespace;
            },
            error: (err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertErrorClosed = false;
                this.loading = false;
            }
        });
    }

    loadingConfigYaml = false;

    public gridGetConfigurationDetails(config: NamespaceConfigurationResponse): void {
        this.loadingConfigYaml = true;
        this.tenantNamespaceService.getNamespaceConfiguration(this.namespace.id, config.id)
            .subscribe({
                next: (detailConfigState) => {
                    this.gridNamespaceConfigDetailState = detailConfigState;
                    this.loadingConfigYaml = false
                },
                error: (err) => {
                    this.errorMessage = resolveErrorMessage(err);
                    this.alertErrorClosed = false;
                    this.loadingConfigYaml = false;                }
            });
    }

    public subscribeToConfigurationsGrid(): void {
        this.onDataGridRefresh.pipe(
            debounceTime(500),
            filter((state => !!state)),
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
                return this.tenantNamespaceService.getNamespaceConfigurations(this.namespace.id, this.restQuery);
            })).subscribe({
            next: (response) => {
                this.configurationsPage = response;
                this.loading = false;

            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        });
    }

    public refreshByGrid(state: ClrDatagridStateInterface): void {
        this.onDataGridRefresh.next(state);
    }
}
