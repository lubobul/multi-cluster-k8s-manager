import {Component, OnInit} from '@angular/core';
import {ClusterService} from '../services/cluster.service';
import {ClarityModule, ClrAlertModule} from '@clr/angular';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {ClusterResponse} from '../../common/rest/types/provider/responses/ClusterResponse';
import {resolveErrorMessage} from '../../common/utils/util-functions';
import {deepClone} from '@cds/core/internal';
import {DatePipe} from '@angular/common';
import {EditorComponent} from 'ngx-monaco-editor-v2';

@Component({
  selector: 'app-kubernetes-clusters',
    imports: [
        ClrAlertModule,
        ClarityModule,
        DatePipe,
        EditorComponent
    ],
  templateUrl: './kubernetes-clusters.component.html',
  styleUrl: './kubernetes-clusters.component.scss'
})
export class KubernetesClustersComponent implements OnInit{
    errorMessage = "";
    alertClosed = true;
    loading = true;
    openRegisterClusterModal = false;

    allClusters: ClusterResponse[] = [];

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    public editorOptions = {theme: "vs", language: "yaml", automaticLayout: true, minimap: {enabled: false}};


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
                this.allClusters = allClusters;
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
                this.loading = false;
            }
        })
    }

    registerCluster(): void{

    }

}
