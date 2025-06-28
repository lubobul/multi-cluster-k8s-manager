import {Component, OnInit} from '@angular/core';
import {
    ClrAccordionModule,
    ClrAlertModule, ClrCommonFormsModule,
    ClrDatagridModule,
    ClrDatagridStateInterface,
    ClrIconModule, ClrInputModule, ClrSidePanelModule,
    ClrSpinnerModule, ClrStepperModule, ClrTextareaModule, ClrVerticalNavModule
} from '@clr/angular';
import {EditorComponent} from 'ngx-monaco-editor-v2';
import {BehaviorSubject, debounceTime, filter, mergeMap} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {QueryRequest, QueryRequestSortType} from '../../common/rest/types/requests/query-request';
import {buildRestGridFilter, resolveErrorMessage} from '../../common/utils/util-functions';
import {TemplateCatalog} from '../../common/rest/types/tenant/responses/TemplateCatalog';
import {TenantCatalogService} from '../services/tenant-catalog.service';
import {CdsModule} from '@cds/angular';
import {CreateCatalogRequest} from '../../common/rest/types/tenant/requests/CreateCatalogRequest';
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';

@Component({
    selector: 'app-template-catalogs',
    imports: [
        ClrAlertModule,
        ClrDatagridModule,
        ClrSpinnerModule,
        EditorComponent,
        ClrIconModule,
        ClrVerticalNavModule,
        CdsModule,
        ClrAccordionModule,
        ClrCommonFormsModule,
        ClrInputModule,
        ClrSidePanelModule,
        ClrStepperModule,
        ClrTextareaModule,
        FormsModule,
        ReactiveFormsModule
    ],
    templateUrl: './template-catalogs.component.html',
    styleUrl: './template-catalogs.component.scss'
})
export class TemplateCatalogsComponent implements OnInit {
    private onDataGridRefresh = new BehaviorSubject<ClrDatagridStateInterface>(null);
    errorMessage = "";
    alertErrorClosed = true;
    loading = true;
    catalogsPage: PaginatedResponse<TemplateCatalog> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<TemplateCatalog>;

    createCatalogModalOpened = false;

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    catalogForm: FormGroup<{
        name: FormControl<string>,
        description: FormControl<string>,

    }>;

    constructor(
        private catalogService: TenantCatalogService,
        private fb: FormBuilder,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.subscribeToConfigurationsGrid();

        this.catalogForm = this.fb.group({
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
                return this.catalogService.getCatalogs(this.restQuery);
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

    public refreshByGrid(state: ClrDatagridStateInterface): void {
        this.onDataGridRefresh.next(state);
    }

    public openCreateCatalogModal(): void {
        this.createCatalogModalOpened = true;
    }

    public deleteCatalog(catalogId: number): void {
        this.loading = true;
        this.createCatalogModalOpened = false;
        this.catalogService.deleteCatalog(catalogId).subscribe({
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
        this.catalogService.createCatalog({
            name: this.catalogForm.controls.name.value,
            description: this.catalogForm.controls.description.value,
        } as CreateCatalogRequest).subscribe({
            next: (response) => {
                this.refreshByGrid({});
            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertErrorClosed = false;
            }
        })
    }
}
