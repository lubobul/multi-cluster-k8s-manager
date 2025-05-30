import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClusterService} from '../services/cluster.service';
import {ClarityModule, ClrAlertModule} from '@clr/angular';
import {QueryRequest} from '../../common/rest/types/requests/query-request';
import {ClusterResponse} from '../../common/rest/types/provider/responses/ClusterResponse';
import {resolveErrorMessage} from '../../common/utils/util-functions';
import {deepClone} from '@cds/core/internal';
import {DatePipe} from '@angular/common';
import {EditorComponent} from 'ngx-monaco-editor-v2';
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {BehaviorSubject, delayWhen, mergeMap, retry, Subscription, tap, timer} from 'rxjs';
import {RegisterClusterRequest} from '../../common/rest/types/provider/requests/RegisterClusterRequest';

@Component({
    selector: 'app-kubernetes-clusters',
    imports: [
        ClrAlertModule,
        ClarityModule,
        DatePipe,
        EditorComponent,
        FormsModule,
        ReactiveFormsModule
    ],
    templateUrl: './kubernetes-clusters.component.html',
    styleUrl: './kubernetes-clusters.component.scss'
})
export class KubernetesClustersComponent implements OnInit, OnDestroy {
    errorMessage = "";
    alertErrorClosed = true;

    errorRegisterClusterMessage = "";
    alertErrorRegisterClusterClosed = true;

    loading = true;
    loadingRegisterCluster = false;
    registerClusterModalOpened = false;

    allClusters: ClusterResponse[] = [];

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    public editorOptions = {theme: "vs", language: "yaml", automaticLayout: true, minimap: {enabled: false}};

    private readonly kubeConfigExample = `apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: certificateText
        server: https://127.0.0.1:6443
      name: cluster-name
    contexts:
    - context:
        cluster: rancher-desktop
        user: rancher-desktop
      name: rancher-desktop
    current-context: rancher-desktop
    kind: Config
    preferences: {}
    users:
    - name: rancher-desktop
      user:
        client-certificate-data: clientCertificateText
        client-key-data: clientKeyText`;

    clusterForm: FormGroup<{
        details: FormGroup<{
            name: FormControl<string>,
            description: FormControl<string>,
        }>,
        config: FormGroup<{
            kubeConfig: FormControl<string>,
        }>
    }>;

    constructor(
        private clusterService: ClusterService,
        private fb: FormBuilder,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.loadClusters();
        this.startClustersPolling(5000);
        this.buildForm();
    }

    ngOnDestroy(): void{
        this.stopClustersPolling();
    }

    buildForm(): void{
        this.clusterForm = this.fb.group({
            details: this.fb.group({
                name: ["", Validators.required],
                description: ["", Validators.required],
            }),
            config: this.fb.group({
                kubeConfig: [this.kubeConfigExample, Validators.required]
            }),
        });
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

    openRegisterClusterModal(): void{
        this.registerClusterModalOpened = true;
        this.clusterForm.controls.details.reset({
            name: "",
            description: "",
        });

        this.clusterForm.controls.config.reset({
            kubeConfig: this.kubeConfigExample,
        });
    }

    registerCluster(): void {

        this.loadingRegisterCluster = true;
        this.clusterService.registerCluster({
            name: this.clusterForm.controls.details.controls.name.value,
            description: this.clusterForm.controls.details.controls.description.value,
            kubeconfig: this.clusterForm.controls.config.controls.kubeConfig.value,
        } as RegisterClusterRequest).subscribe(
            {
                next: (allClusters) => {
                    this.loadingRegisterCluster = false;
                    this.loadClusters();
                    this.registerClusterModalOpened = false;
                },
                error: (error) => {
                    this.errorMessage = resolveErrorMessage(error);
                    this.alertErrorRegisterClusterClosed = false;
                    this.loadingRegisterCluster = false;
                }
            }
        );
    }
}
