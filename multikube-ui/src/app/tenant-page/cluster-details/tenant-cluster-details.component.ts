import {Component, OnInit} from '@angular/core';
import {ClarityModule} from '@clr/angular';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TENANT_ROUTE_PATHS} from '../../app.routes';
import {TenantClusterDetailsService} from '../services/tenant-cluster-details.service';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';
import {TenantNamespaceService} from '../services/tenant-namespace.service';
import {TenantNamespaceSummaryResponse} from '../../common/rest/types/tenant/responses/TenantNamespace';
import {catchError, mergeMap, tap} from 'rxjs';
import {resolveErrorMessage} from '../../common/utils/util-functions';

@Component({
    selector: 'app-cluster-details',
    imports: [
        ClarityModule,
        RouterOutlet,
        RouterLink,
        RouterLinkActive
    ],
    providers: [
        TenantClusterDetailsService
    ],
    templateUrl: './tenant-cluster-details.component.html',
    styleUrl: './tenant-cluster-details.component.scss'
})
export class TenantClusterDetailsComponent implements OnInit {
    protected readonly TENANT_ROUTE_PATHS = TENANT_ROUTE_PATHS;
    namespaces: TenantNamespaceSummaryResponse[] = [];
    cluster: TenantClusterResponse;

    loading = false;
    errorMessage = "";
    alertErrorClosed = true;

    constructor(
        private clusterDetailsService: TenantClusterDetailsService,
        private namespaceService: TenantNamespaceService,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.clusterDetailsService.cluster$.pipe(
            tap((cluster) => {
                this.cluster = cluster;
            }),
            mergeMap((cluster) => {
                return this.namespaceService.getAllNamespaceSummaries(
                    cluster.id,
                    {
                        page: 1,
                        pageSize: 5,
                    });
            }),
            tap((namespaces) => {
                this.namespaces = namespaces;
                this.loading = false;
            }),
            catchError((err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertErrorClosed = false;
                this.loading = false;
                return err;
            })
        ).subscribe();

        this.clusterDetailsService.cluster$.subscribe({
            next: (cluster) => {
                this.cluster = cluster;

            },
            error: (err) => {
            }
        });
    }
}
