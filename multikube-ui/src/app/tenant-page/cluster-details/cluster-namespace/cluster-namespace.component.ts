import {Component, OnInit} from '@angular/core';
import {ClarityModule, ClrAlertModule, ClrSpinnerModule} from "@clr/angular";
import {ClusterStatusComponent} from "../../../common/cluster-status/cluster-status.component";
import {DatePipe} from "@angular/common";
import {ClusterResponse} from '../../../common/rest/types/provider/responses/ClusterResponse';
import {ClusterDetailsService} from '../../../provider-page/services/cluster-details.service';
import {resolveErrorMessage} from '../../../common/utils/util-functions';
import {TenantNamespaceService} from '../../services/tenant-namespace.service';
import {TenantNamespaceResponse} from '../../../common/rest/types/tenant/responses/TenantNamespace';
import {map, mergeMap, Observable} from 'rxjs';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {TENANT_ROUTE_PATHS} from '../../../app.routes';
import {NamespaceStatusComponent} from '../../../common/namespace-status/namespace-status.component';

@Component({
    selector: 'app-cluster-namespaces',
    imports: [
        ClrAlertModule,
        ClrSpinnerModule,
        DatePipe,
        NamespaceStatusComponent,
        ClarityModule
    ],
    templateUrl: './cluster-namespace.component.html',
    styleUrl: './cluster-namespace.component.scss'
})
export class ClusterNamespaceComponent implements OnInit {
    loading = false;
    errorMessage = "";
    alertErrorClosed = true;
    namespace: TenantNamespaceResponse;

    constructor(
        private namespaceService: TenantNamespaceService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        (this.activatedRoute.params as Observable<Params>).pipe(
            map((routeParameters) => {
                return routeParameters[TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_ID];
            }),
            mergeMap((namespaceId) => {
                return this.namespaceService.getNamespace(namespaceId);
            })
        ).subscribe({
            next: (namespace) => {
                this.namespace = namespace;
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertErrorClosed = false;
                this.loading = false;
            }
        });
    }
}
