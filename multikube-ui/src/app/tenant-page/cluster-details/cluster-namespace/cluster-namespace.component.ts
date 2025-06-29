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
import {ActivatedRoute, Params, Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TENANT_ROUTE_PATHS} from '../../../app.routes';
import {NamespaceStatusComponent} from '../../../common/namespace-status/namespace-status.component';
import {TenantNamespaceDetailsService} from '../../services/tenant-namespace-details.service';
import {CdsModule} from '@cds/angular';

@Component({
    selector: 'app-cluster-namespaces',
    imports: [
        ClrAlertModule,
        ClrSpinnerModule,
        ClarityModule,
        RouterLink,
        RouterLinkActive,
        RouterOutlet,
        CdsModule
    ],
    providers: [
        TenantNamespaceDetailsService,
    ],
    templateUrl: './cluster-namespace.component.html',
    styleUrl: './cluster-namespace.component.scss'
})
export class ClusterNamespaceComponent {

    namespace: TenantNamespaceResponse;
    constructor(
        private namespaceDetailsService: TenantNamespaceDetailsService
    ) {
        this.namespaceDetailsService.namespace$.subscribe((namespace) => {
            this.namespace = namespace;
        })
    }

    protected readonly TENANT_ROUTE_PATHS = TENANT_ROUTE_PATHS;
}
