import { Component } from '@angular/core';
import {ClarityModule} from '@clr/angular';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {TENANT_ROUTE_PATHS} from '../../app.routes';
import {TenantClusterDetailsService} from '../services/tenant-cluster-details.service';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';

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
export class TenantClusterDetailsComponent {
    protected readonly TENANT_ROUTE_PATHS = TENANT_ROUTE_PATHS;

    cluster: TenantClusterResponse;
    constructor(private clusterDetailsService: TenantClusterDetailsService) {
        this.clusterDetailsService.cluster$.subscribe({
            next: (cluster) => {
                this.cluster = cluster;

            },
            error: (err) => {
            }
        });
    }
}
