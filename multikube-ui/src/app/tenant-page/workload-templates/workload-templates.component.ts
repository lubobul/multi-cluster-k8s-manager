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
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {TemplateCatalog} from '../../common/rest/types/tenant/responses/TemplateCatalog';
import {QueryRequest, QueryRequestSortType} from '../../common/rest/types/requests/query-request';
import {TenantTemplateService} from '../services/tenant-template.service';
import {buildRestGridFilter, resolveErrorMessage} from '../../common/utils/util-functions';
import {CreateCatalogRequest} from '../../common/rest/types/tenant/requests/CreateCatalogRequest';
import {TemplateType, WorkloadTemplate} from '../../common/rest/types/tenant/responses/WorkloadTemplate';
import {CreateWorkloadTemplateRequest} from '../../common/rest/types/tenant/requests/CreateTemplateRequest';
import {DatePipe} from '@angular/common';
import {EditorComponent} from 'ngx-monaco-editor-v2';
import {TenantCatalogService} from '../services/tenant-catalog.service';
import {NamespaceConfigurationResponse} from '../../common/rest/types/tenant/responses/TenantNamespaceResources';

@Component({
    selector: 'workload-templates',
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
    ],
    templateUrl: './workload-templates.component.html',
    styleUrl: './workload-templates.component.scss'
})
export class WorkloadTemplatesComponent implements OnInit {
    private onTemplatesDataGridRefresh = new BehaviorSubject<ClrDatagridStateInterface>(null);
    private onCatalogsDataGridRefresh = new BehaviorSubject<ClrDatagridStateInterface>(null);
    errorMessage = "";
    alertErrorClosed = true;
    loading = true;
    templatesPage: PaginatedResponse<WorkloadTemplate> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<WorkloadTemplate>;

    createCatalogModalOpened = false;

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    public editorOptions = {theme: "vs", language: "yaml", automaticLayout: true, minimap: {enabled: false}};


    templateForm: FormGroup<{
        details: FormGroup<{
            name: FormControl<string>,
            description: FormControl<string>,
            type: FormControl<TemplateType>,
        }>,
        catalogSelection: FormGroup<{
            catalogId: FormControl<number | null>,
        }>,
        yamlConfig: FormGroup<{
            yaml: FormControl<string>,
        }>
    }>;

    initialStateform: any;

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
        - containerPort: 80`;

    selectedCatalog: TemplateCatalog;

    gridWorkloadTemplateDetailState: WorkloadTemplate;
    public templateViewEditorOptions = {
        theme: "vs",
        language: "yaml",
        automaticLayout: true,
        readOnly: true,
        minimap: {enabled: false}
    };

    loadingTemplateYaml = false;

    constructor(
        private templateService: TenantTemplateService,
        private fb: FormBuilder,
        private catalogService: TenantCatalogService,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.subscribeToTemplatesGrid();
        this.subscribeToCatalogGrid();

        // @ts-ignore
        this.templateForm = this.fb.group({
            details: this.fb.group({
                name: ['', Validators.required],
                description: [''],
                type: [TemplateType.YAML, Validators.required],
            }),
            catalogSelection: this.fb.group({
                catalogId: [null, Validators.required],
            }),
            yamlConfig: this.fb.group({
                yaml: [this.deploymentExample, Validators.required],
            })
        });

        this.initialStateform = this.templateForm.value;
    }

    public subscribeToTemplatesGrid(): void {
        this.onTemplatesDataGridRefresh.pipe(
            debounceTime(500),
            filter((state => !!state)),
            mergeMap((state) => {
                this.loading = true;
                this.restQuery = {
                    pageSize: state?.page?.size || 5,
                    page: state?.page?.current || 1,
                    sort: state?.sort ? {
                        sortField: state.sort.by as string,
                        sortType: state.sort.reverse ? QueryRequestSortType.DESC : QueryRequestSortType.ASC
                    } : undefined,
                    filter: buildRestGridFilter(state.filters)
                }
                return this.templateService.getTemplates(this.restQuery);
            })).subscribe({
            next: (response) => {
                this.templatesPage = response;
                this.loading = false;

            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        });
    }

    public refreshByTemplatesGrid(state: ClrDatagridStateInterface): void {
        this.onTemplatesDataGridRefresh.next(state);
    }

    public refreshByCatalogsGrid(state: ClrDatagridStateInterface): void {
        this.onCatalogsDataGridRefresh.next(state);
    }

    public openCreateCatalogModal(): void {
        // @ts-ignore
        this.templateForm = this.fb.group({
            details: this.fb.group({
                name: ['', Validators.required],
                description: [''],
                type: [TemplateType.YAML, Validators.required],
            }),
            catalogSelection: this.fb.group({
                catalogId: [null, Validators.required],
            }),
            yamlConfig: this.fb.group({
                yaml: [this.deploymentExample, Validators.required],
            })
        });
        this.createCatalogModalOpened = true;
    }

    public deleteTemplate(templateId: number): void {
        this.loading = true;
        this.createCatalogModalOpened = false;
        this.templateService.deleteTemplate(templateId).subscribe({
            next: (response) => {
                this.refreshByTemplatesGrid({});
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        })
    }

    public createTemplate(): void {
        this.loading = true;
        this.createCatalogModalOpened = false;
        this.templateService.createTemplate({
            name: this.templateForm.controls.details.controls.name.value,
            description: this.templateForm.controls.details.controls.description.value,
            templateType: this.templateForm.controls.details.controls.type.value,
            catalogId: this.templateForm.controls.catalogSelection.controls.catalogId.value,
            yamlContent: this.templateForm.controls.yamlConfig.controls.yaml.value,
        } as CreateWorkloadTemplateRequest).subscribe({
            next: (response) => {
                this.refreshByTemplatesGrid({});
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        })
    }

    catalogsPage: PaginatedResponse<TemplateCatalog> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<TemplateCatalog>;

    private catalogsRestQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    public subscribeToCatalogGrid(): void {
        this.onCatalogsDataGridRefresh.pipe(
            debounceTime(500),
            filter((state => !!state)),
            mergeMap((state) => {
                this.loading = true;
                this.catalogsRestQuery = {
                    pageSize: state?.page?.size || 5,
                    page: state?.page?.current || 1,
                    sort: state?.sort ? {
                        sortField: state.sort.by as string,
                        sortType: state.sort.reverse ? QueryRequestSortType.DESC : QueryRequestSortType.ASC
                    } : undefined,
                    filter: buildRestGridFilter(state.filters)
                }
                return this.catalogService.getCatalogs(this.catalogsRestQuery);
            })).subscribe({
            next: (response) => {
                this.catalogsPage = response;
                this.loading = false;

            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
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
                    this.loadingTemplateYaml = false
                },
                error: (err) => {
                    this.errorMessage = resolveErrorMessage(err);
                    this.alertErrorClosed = false;
                    this.loadingTemplateYaml = false;
                }
            });
    }

    protected catalogSelected(catalog: TemplateCatalog): void {
        this.templateForm.controls.catalogSelection.controls.catalogId.setValue(catalog?.id);
    }

    protected readonly TemplateType = TemplateType;
}
