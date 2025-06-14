import {ActivatedRoute, Params, Router} from '@angular/router';
import {TenantClusterService} from './tenant-cluster.service';
import {map, mergeMap, Observable, ReplaySubject} from 'rxjs';
import {MULTIKUBE_ROUTE_PATHS, TENANT_ROUTE_PATHS} from '../../app.routes';
import {Injectable} from '@angular/core';
import {TenantClusterResponse} from '../../common/rest/types/tenant/responses/TenantClusterResponse';
import {TenantNamespaceService} from './tenant-namespace.service';
import {TenantNamespaceResponse} from '../../common/rest/types/tenant/responses/TenantNamespace';

@Injectable()
export class TenantNamespaceDetailsService {

    private namespaceSubj = new ReplaySubject<TenantNamespaceResponse>(1);
    public namespace$ = this.namespaceSubj.asObservable();

    constructor(
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private namespaceService: TenantNamespaceService,
    ) {
        this.updateNamespaceDetails();
    }

    public updateNamespaceDetails(): void {
        (this.activatedRoute.params as Observable<Params>).pipe(
            map((routeParameters) => {
                return routeParameters[TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACE_ID];
            }),
            mergeMap((namespaceId) => {
                return this.namespaceService.getNamespace(namespaceId);
            })
        ).subscribe({
            next: (cluster) => {
                this.namespaceSubj.next(cluster);
            },
            error: (err) => {
                this.namespaceSubj.error(err);
            }
        });
    }
}
