import {ActivatedRoute, Params, Router} from '@angular/router';
import {TenantClusterService} from './tenant-cluster.service';
import {map, mergeMap, Observable, ReplaySubject} from 'rxjs';
import {MULTIKUBE_ROUTE_PATHS, TENANT_ROUTE_PATHS} from '../../app.routes';
import {Injectable} from '@angular/core';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';

@Injectable()
export class TenantClusterDetailsService {

    private clusterSubj = new ReplaySubject<TenantClusterResponse>(1);
    public cluster$ = this.clusterSubj.asObservable();

    constructor(
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private clusterService: TenantClusterService,
    ) {
        this.updateClusterDetails();
    }

    public updateClusterDetails(): void {
        (this.activatedRoute.params as Observable<Params>).pipe(
            map((routeParameters) => {
                return routeParameters[TENANT_ROUTE_PATHS.CLUSTER_ID];
            })
        ).pipe(
            mergeMap((clusterId) => {
                return this.clusterService.getCluster(clusterId);
            })).subscribe({
            next: (cluster) => {
                this.clusterSubj.next(cluster);
            },
            error: (err) => {
                this.clusterSubj.error(err);
            }
        });
    }
}
