import {Component, OnInit} from '@angular/core';
import {CdsIconModule} from '@cds/angular';
import {
    ClrAlertModule,
    ClrCommonFormsModule,
    ClrDatagridModule, ClrDatagridStateInterface,
    ClrIconModule,
    ClrInputModule,
    ClrSidePanelModule, ClrTextareaModule
} from '@clr/angular';
import {FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
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
        DatePipe
    ],
  templateUrl: './workload-templates.component.html',
  styleUrl: './workload-templates.component.scss'
})
export class WorkloadTemplatesComponent implements OnInit {
    private onDataGridRefresh = new BehaviorSubject<ClrDatagridStateInterface>(null);
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

    templateForm: FormGroup<{
        name: FormControl<string>,
        description: FormControl<string>,
    }>;

    constructor(
        private templateService: TenantTemplateService,
        private fb: FormBuilder,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.subscribeToConfigurationsGrid();

        this.templateForm = this.fb.group({
            name: ['', Validators.required],
            description: ['', Validators.required],
        })
    }

    public subscribeToConfigurationsGrid(): void {
        this.onDataGridRefresh.pipe(
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

    public refreshByGrid(state: ClrDatagridStateInterface): void {
        this.onDataGridRefresh.next(state);
    }

    public openCreateCatalogModal(): void {
        this.createCatalogModalOpened = true;
    }

    public deleteTemplate(templateId: number): void {
        this.loading = true;
        this.createCatalogModalOpened = false;
        this.templateService.deleteTemplate(templateId).subscribe({
            next: (response) => {
                this.refreshByGrid({});
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        })
    }

    public createCatalog(): void {
        this.loading = true;
        this.createCatalogModalOpened = false;
        this.templateService.createTemplate({
            name: this.templateForm.controls.name.value,
            description: this.templateForm.controls.description.value,
        } as CreateWorkloadTemplateRequest).subscribe({
            next: (response) => {
                this.refreshByGrid({});
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        })
    }

    protected readonly TemplateType = TemplateType;
}
