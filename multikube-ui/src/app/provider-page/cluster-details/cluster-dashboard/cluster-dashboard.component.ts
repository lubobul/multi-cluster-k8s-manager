import {Component, OnInit} from '@angular/core';
import {ClusterDetailsService} from '../../services/cluster-details.service';
import {ClusterResponse} from '../../../common/rest/types/provider/responses/ClusterResponse';
import {ClrAlertModule, ClrSpinnerModule} from '@clr/angular';
import {resolveErrorMessage} from '../../../common/utils/util-functions';
import {ClusterStatusComponent} from '../../../common/cluster-status/cluster-status.component';
import {DatePipe} from '@angular/common';

@Component({
    selector: 'app-cluster-dashboard',
    imports: [
        ClrAlertModule,
        ClrSpinnerModule,
        ClusterStatusComponent,
        DatePipe
    ],
    templateUrl: './cluster-dashboard.component.html',
    styleUrl: './cluster-dashboard.component.scss'
})
export class ClusterDashboardComponent implements OnInit{
    loading = false;
    errorMessage = "";
    alertErrorClosed = true;
    cluster: ClusterResponse;

    constructor(
        private clusterDetailsService: ClusterDetailsService,
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
