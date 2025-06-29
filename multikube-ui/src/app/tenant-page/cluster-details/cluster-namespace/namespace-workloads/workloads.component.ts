import {Component, OnInit} from '@angular/core';
import {CdsIconModule} from '@cds/angular';
import {
    ClarityModule,
    ClrAccordionModule,
    ClrAlertModule,
    ClrCommonFormsModule,
    ClrDatagridModule, ClrDatagridStateInterface,
    ClrIconModule,
    ClrInputModule, ClrRadioModule,
    ClrSidePanelModule, ClrStepperModule, ClrTextareaModule
} from '@clr/angular';
import {Form, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {BehaviorSubject, debounceTime, filter, mergeMap} from 'rxjs';
import {PaginatedResponse} from '../../../../common/rest/types/responses/paginated-response';
import {QueryRequest, QueryRequestSortType} from '../../../../common/rest/types/requests/query-request';
import {TenantTemplateService} from '../../../services/tenant-template.service';
import {buildRestGridFilter, resolveErrorMessage} from '../../../../common/utils/util-functions';
import {TemplateType, WorkloadTemplate} from '../../../../common/rest/types/tenant/responses/WorkloadTemplate';
import {CreateWorkloadTemplateRequest} from '../../../../common/rest/types/tenant/requests/CreateTemplateRequest';
import {DatePipe} from '@angular/common';
import {EditorComponent} from 'ngx-monaco-editor-v2';
import {TenantWorkloadService} from '../../../services/tenant-workload.service';
import {TenantNamespaceDetailsService} from '../../../services/tenant-namespace-details.service';
import {TenantNamespaceResponse} from '../../../../common/rest/types/tenant/responses/TenantNamespace';
import {k8sResourceStatusComponent} from '../../../../common/k8s-resource-status/k8s-resource-status.component';
import {k8sResourceSyncStatusComponent} from '../../../../common/k8s-sync-status/k8s-resource-sync-status.component';
import {WorkloadResponse} from '../../../../common/rest/types/tenant/responses/WorkloadResponse';
import {CreateWorkloadRequest} from '../../../../common/rest/types/tenant/requests/CreateWorkloadRequest';

export enum WorkloadType{
    FromTemplate = 'FromTemplate',
    FromScratch = 'FromScratch',
}

@Component({
    selector: 'workloads',
    imports: [
        CdsIconModule,
        ClrAlertModule,
        ClrCommonFormsModule,
        ClrDatagridModule,
        ClrIconModule,
        ClrInputModule,
        ClrSidePanelModule,
        ClrTextareaModule,
        ReactiveFormsModule,
        DatePipe,
        ClrAccordionModule,
        ClrRadioModule,
        ClrStepperModule,
        EditorComponent,
        ClarityModule,
        FormsModule,
        k8sResourceStatusComponent,
        k8sResourceSyncStatusComponent,
    ],
    templateUrl: './workloads.component.html',
    styleUrl: './workloads.component.scss'
})
export class WorkloadsComponent implements OnInit {
    private onTemplatesDataGridRefresh = new BehaviorSubject<ClrDatagridStateInterface>(null);
    private onWorkloadsDataGridRefresh = new BehaviorSubject<ClrDatagridStateInterface>(null);

    errorMessage = "";
    alertErrorClosed = true;
    loading = true;
    workloadsPage: PaginatedResponse<WorkloadResponse> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<WorkloadResponse>;

    private workloadsRestQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    templatesGridLoading = false;
    loadingTemplateYaml = false;

    templatesPage: PaginatedResponse<WorkloadTemplate> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<WorkloadTemplate>;

    private templatesRestQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    gridWorkloadDetailState: WorkloadResponse;

    createWorkloadModalOpened = false;

    public editorOptions = {theme: "vs", language: "yaml", automaticLayout: true, minimap: {enabled: false}};


    workloadForm: FormGroup<{
        details: FormGroup<{
            name: FormControl<string>,
            description: FormControl<string>,
            type: FormControl<WorkloadType>,
        }>,
        templateSelection: FormGroup<{
            templateId: FormControl<number | null>,
        }>,
        yamlConfig: FormGroup<{
            yaml: FormControl<string>,
        }>
    }>;

    private readonly deploymentExample = `apiVersion: apps/v1
kind: Deployment
metadata:
  # The name of the Deployment resource itself.
  # A user might edit this to be unique for their application instance.
  name: my-nginx-deployment
  labels:
    # Labels are used to organize resources.
    # They can be used to select which deployments to view or manage.
    app: my-nginx
spec:
  # The desired number of running pod instances.
  replicas: 2
  selector:
    # This selector tells the Deployment which pods it manages.
    # It must match the labels in the pod template below.
    matchLabels:
      app: my-nginx
  template:
    metadata:
      # These labels are applied to each pod created by this Deployment.
      labels:
        app: my-nginx
    spec:
      containers:
      - name: nginx-container
        # The container image to run. Using a specific, lightweight version is best practice.
        image: nginx:1.21-alpine
        ports:
        # The port that the container exposes.
        - containerPort: 80`

    selectedWorkloadTemplate?: WorkloadTemplate;

    public templateViewEditorOptions = {
        theme: "vs",
        language: "yaml",
        automaticLayout: true,
        readOnly: true,
        minimap: {enabled: false}
    };

    loadingWorkloadYaml = false;
    namespace: TenantNamespaceResponse;
    gridWorkloadTemplateDetailState: WorkloadTemplate;

    constructor(
        private workloadService: TenantWorkloadService,
        private templateService: TenantTemplateService,
        private namespaceDetailsService: TenantNamespaceDetailsService,
        private fb: FormBuilder,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;

        this.namespaceDetailsService.namespace$.subscribe({
            next: (namespace) => {
                this.namespace = namespace;
                this.subscribeToTemplatesGrid();
                this.subscribeToWorkloadsGrid();
            },
            error: (err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertErrorClosed = false;
                this.loading = false;
            }
        });

        // @ts-ignore
        this.workloadForm = this.fb.group({
            details: this.fb.group({
                name: ['', Validators.required],
                description: [''],
                type: [WorkloadType.FromTemplate, Validators.required],
            }),
            templateSelection: this.fb.group({
                templateId: [null, Validators.required],
            }),
            yamlConfig: this.fb.group({
                yaml: [this.deploymentExample, Validators.required],
            })
        });
    }

    public subscribeToTemplatesGrid(): void {
        this.onTemplatesDataGridRefresh.pipe(
            debounceTime(500),
            filter((state => !!state)),
            mergeMap((state) => {
                this.templatesGridLoading = true;
                this.templatesRestQuery = {
                    pageSize: state?.page?.size || 5,
                    page: state?.page?.current || 1,
                    sort: state?.sort ? {
                        sortField: state.sort.by as string,
                        sortType: state.sort.reverse ? QueryRequestSortType.DESC : QueryRequestSortType.ASC
                    } : undefined,
                    filter: buildRestGridFilter(state.filters)
                }
                return this.templateService.getTemplates(this.templatesRestQuery);
            })).subscribe({
            next: (response) => {
                this.templatesPage = response;
                this.templatesGridLoading = false;

            }, error: (error) => {
                this.templatesGridLoading = false;
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        });
    }

    public subscribeToWorkloadsGrid(): void {
        this.onWorkloadsDataGridRefresh.pipe(
            debounceTime(500),
            filter((state => !!state)),
            mergeMap((state) => {
                this.loading = true;
                this.workloadsRestQuery = {
                    pageSize: state?.page?.size || 5,
                    page: state?.page?.current || 1,
                    sort: state?.sort ? {
                        sortField: state.sort.by as string,
                        sortType: state.sort.reverse ? QueryRequestSortType.DESC : QueryRequestSortType.ASC
                    } : undefined,
                    filter: buildRestGridFilter(state.filters)
                }
                return this.workloadService.getWorkloads(this.namespace.id, this.workloadsRestQuery);
            })).subscribe({
            next: (response) => {
                this.workloadsPage = response;
                this.loading = false;

            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        });
    }

    public gridGetWorkloadDetails(workload: WorkloadResponse): void {
        if (!workload) {
            return;
        }
        this.loadingWorkloadYaml = true;
        this.workloadService.getWorkload(this.namespace.id, workload.id)
            .subscribe({
                next: (detailTemplateState) => {
                    this.gridWorkloadDetailState = detailTemplateState;
                    this.loadingWorkloadYaml = false
                },
                error: (err) => {
                    this.errorMessage = resolveErrorMessage(err);
                    this.alertErrorClosed = false;
                    this.loadingWorkloadYaml = false;
                }
            });
    }

    public gridGetTemplateDetails(template: WorkloadTemplate): void {
        if (!template) {
            return;
        }
        this.loadingTemplateYaml = true;
        this.templateService.getTemplate(template.id)
            .subscribe({
                next: (detailTemplateState) => {
                    this.gridWorkloadTemplateDetailState = detailTemplateState;
                    this.workloadForm.controls.yamlConfig.controls.yaml.setValue(detailTemplateState.yamlContent);
                    this.loadingTemplateYaml = false
                },
                error: (err) => {
                    this.errorMessage = resolveErrorMessage(err);
                    this.alertErrorClosed = false;
                    this.loadingTemplateYaml = false;
                }
            });
    }

    public refreshByTemplatesGrid(state: ClrDatagridStateInterface): void {
        this.onTemplatesDataGridRefresh.next(state);
    }

    public refreshByWorkloadsGrid(state: ClrDatagridStateInterface): void {
        this.onWorkloadsDataGridRefresh.next(state);
    }

    public deleteWorkload(workloadId: number): void {
        this.loading = true;
        this.workloadService.deleteWorkload(this.namespace.id, workloadId).subscribe({
            next: (response) => {
                this.refreshByWorkloadsGrid({});
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        })
    }

    public openCreateWorkloadModal(): void {
        this.workloadForm.reset();
        // @ts-ignore
        this.workloadForm = this.fb.group({
            details: this.fb.group({
                name: ['', Validators.required],
                description: [''],
                type: [WorkloadType.FromTemplate, Validators.required],
            }),
            templateSelection: this.fb.group({
                templateId: [null, Validators.required],
            }),
            yamlConfig: this.fb.group({
                yaml: [this.deploymentExample, Validators.required],
            })
        });
        this.createWorkloadModalOpened = true;
    }

    public createWorkload(): void {
        this.loading = true;
        this.createWorkloadModalOpened = false;
        this.workloadService.createWorkload(this.namespace.id, {
            name: this.workloadForm.controls.details.controls.name.value,
            description: this.workloadForm.controls.details.controls.description.value,
            yamlContent: this.workloadForm.controls.yamlConfig.controls.yaml.value,
        } as CreateWorkloadRequest).subscribe({
            next: (response) => {
                this.refreshByWorkloadsGrid({});
            }, error: (error) => {
                this.refreshByWorkloadsGrid({});
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        })
    }

    protected workloadTemplateSelected(template: WorkloadTemplate): void {
        this.workloadForm.controls.templateSelection.controls.templateId.setValue(template?.id);
    }

    protected readonly TemplateType = TemplateType;
    protected readonly WorkloadType = WorkloadType;
}
