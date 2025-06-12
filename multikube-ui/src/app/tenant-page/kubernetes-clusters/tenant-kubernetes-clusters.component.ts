import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClarityModule, ClrAlertModule} from '@clr/angular';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {resolveErrorMessage} from '../../common/utils/util-functions';
import {DatePipe} from '@angular/common';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {BehaviorSubject, delayWhen, retry, Subscription, tap, timer} from 'rxjs';
import {TENANT_ROUTE_PATHS} from '../../app.routes';
import {ActivatedRoute, Router} from '@angular/router';
import {ClusterStatusComponent} from '../../common/cluster-status/cluster-status.component';
import {TenantClusterService} from '../services/tenant-cluster.service';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';

@Component({
    selector: 'app-kubernetes-clusters',
    imports: [
        ClrAlertModule,
        ClarityModule,
        DatePipe,
        FormsModule,
        ReactiveFormsModule,
        ClusterStatusComponent
    ],
    templateUrl: './tenant-kubernetes-clusters.component.html',
    styleUrl: './tenant-kubernetes-clusters.component.scss'
})
export class TenantKubernetesClustersComponent implements OnInit, OnDestroy {
    errorMessage = "";
    alertErrorClosed = true;

    loading = true;

    allClusters: TenantClusterResponse[] = [];

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    constructor(
        private clusterService: TenantClusterService,
        private fb: FormBuilder,
        private router: Router,
        private activatedRoute: ActivatedRoute,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.loadClusters();
        this.startClustersPolling(5000);
    }

    ngOnDestroy(): void {
        this.stopClustersPolling();
    }

    loadClusters(): void {
        this.clusterService.getAllClusters(this.restQuery).subscribe({
            next: (allClusters) => {
                this.allClusters = allClusters;
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
                this.loading = false;
            }
        })
    }

    private pollingSbj: BehaviorSubject<number>;
    private pollingSubscription: Subscription;

    private startClustersPolling(interval: number): void {
        this.stopClustersPolling();
        this.pollingSbj = new BehaviorSubject<number>(interval);

        this.pollingSubscription = this.pollingSbj.pipe(
            delayWhen((interval) => timer(interval)),
            tap(() => {
                return this.loadClusters();
            }),
            tap(() => {
                this.pollingSbj.next(interval);
            }),
            retry(),
        ).subscribe();
    }

    private stopClustersPolling(): void {
        if (this.pollingSbj) {
            this.pollingSbj.complete();
        }

        if (this.pollingSubscription) {
            this.pollingSubscription.unsubscribe();
        }
    }

    openDetails(cluster: TenantClusterResponse): void {
        this.router.navigate(
            [`${cluster.id}/${TENANT_ROUTE_PATHS.CLUSTER_DETAILS}`],
            {
                relativeTo: this.activatedRoute,
            }
        );
    }
}
