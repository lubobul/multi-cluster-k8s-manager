import {Component, OnInit} from '@angular/core';
import {ClusterService} from '../services/cluster.service';
import {ClarityModule, ClrAlertModule} from '@clr/angular';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {ClusterResponse} from '../../common/rest/types/provider/responses/ClusterResponse';
import {resolveErrorMessage} from '../../common/utils/util-functions';

@Component({
  selector: 'app-kubernetes-clusters',
    imports: [
        ClrAlertModule,
        ClarityModule
    ],
  templateUrl: './kubernetes-clusters.component.html',
  styleUrl: './kubernetes-clusters.component.scss'
})
export class KubernetesClustersComponent implements OnInit{
    errorMessage = "";
    alertClosed = true;
    loading = true;

    allClusters: ClusterResponse[] = [];

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    constructor(
        private clusterService: ClusterService
    ) {
    }

    ngOnInit(): void {
        this.loadClusters();
    }

    loadClusters(): void{
        this.loading = true;
        this.clusterService.getAllClusters(this.restQuery).subscribe({
            next: (allClusters) => {
                this.allClusters = [...allClusters, ...allClusters,  ...allClusters];
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
                this.loading = false;
            }
        })
    }


}
