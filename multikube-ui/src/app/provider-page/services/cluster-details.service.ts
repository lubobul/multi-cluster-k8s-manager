import {ClusterResponse} from '../../common/rest/types/provider/responses/ClusterResponse';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {ClusterService} from './cluster.service';
import {map, mergeMap, Observable, ReplaySubject} from 'rxjs';
import {MULTIKUBE_ROUTE_PATHS} from '../../app.routes';
import {Injectable} from '@angular/core';

@Injectable()
export class ClusterDetailsService {

    private clusterSubj = new ReplaySubject<ClusterResponse>(1);
    public cluster$ = this.clusterSubj.asObservable();

    constructor(
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private clusterService: ClusterService,
    ) {
        this.initClusterDetails();
    }

    public initClusterDetails(): void {
        (this.activatedRoute.params as Observable<Params>).pipe(
            map((routeParameters) => {
                return routeParameters[MULTIKUBE_ROUTE_PATHS.CLUSTER_ID];
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
