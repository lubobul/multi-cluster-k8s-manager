import {Component, OnInit} from '@angular/core';
import {TenantClusterDetailsService} from '../../services/tenant-cluster-details.service';
import {ClrAlertModule, ClrSpinnerModule} from '@clr/angular';
import {resolveErrorMessage} from '../../../common/utils/util-functions';
import {ClusterStatusComponent} from '../../../common/cluster-status/cluster-status.component';
import {DatePipe} from '@angular/common';
import {TenantClusterResponse} from '../../../common/rest/types/tenant/responses/TenantClusterResponse';

@Component({
    selector: 'app-cluster-dashboard',
    imports: [
        ClrAlertModule,
        ClrSpinnerModule,
        ClusterStatusComponent,
        DatePipe
    ],
    templateUrl: './tenant-cluster-dashboard.component.html',
    styleUrl: './tenant-cluster-dashboard.component.scss'
})
export class TenantClusterDashboardComponent implements OnInit{
    loading = false;
    errorMessage = "";
    alertErrorClosed = true;
    cluster: TenantClusterResponse;

    constructor(
        private clusterDetailsService: TenantClusterDetailsService,
    ) {
    }

    ngOnInit(): void{
        this.loading = true;
        this.clusterDetailsService.cluster$.subscribe({
            next: (cluster) => {
                this.cluster = cluster;
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
