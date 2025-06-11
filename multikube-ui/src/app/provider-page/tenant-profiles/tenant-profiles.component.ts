import {Component, OnInit} from '@angular/core';
import {
    ClarityModule,
    ClrAccordionModule,
    ClrAlertModule, ClrCommonFormsModule,
    ClrDatagridModule,
    ClrDatagridStateInterface,
    ClrIconModule, ClrInputModule, ClrSidePanelModule, ClrSpinnerModule, ClrStepperModule, ClrTextareaModule
} from "@clr/angular";
import {DatePipe} from "@angular/common";
import {debounceTime, mergeMap, Subject} from 'rxjs';
import {PaginatedResponse} from '../../common/rest/types/responses/paginated-response';
import {QueryRequest, QueryRequestSortType} from '../../common/rest/types/requests/query-request';
import {buildRestGridFilter, resolveErrorMessage} from '../../common/utils/util-functions';
import {TenantService} from '../services/tenant.service';
import {TenantResponse} from '../../common/rest/types/provider/responses/TenantResponse';
import {
    AbstractControlOptions,
    FormBuilder,
    FormControl,
    FormGroup,
    ReactiveFormsModule,
    Validators
} from '@angular/forms';
import {FormValidators} from '../../common/utils/form-validators';
import {CreateTenantRequest} from '../../common/rest/types/provider/requests/CreateTenantRequest';

@Component({
    selector: 'app-tenant-profiles',
    imports: [
        ClrAlertModule,
        ClrDatagridModule,
        DatePipe,
        ClrIconModule,
        ClrAccordionModule,
        ClrCommonFormsModule,
        ClrInputModule,
        ClrSidePanelModule,
        ClrSpinnerModule,
        ClrStepperModule,
        ClrTextareaModule,
        ReactiveFormsModule,
        ClarityModule,
    ],
    templateUrl: './tenant-profiles.component.html',
    styleUrl: './tenant-profiles.component.scss'
})
export class TenantProfilesComponent implements OnInit {

    private onDataGridRefresh = new Subject<ClrDatagridStateInterface>();
    errorMessage = "";
    alertClosed = true;
    loading = true;

    createTenantModalOpened = false;
    creatingTenantLoading = false;
    createTenantError = "";
    alertErrorCreateTenantClosed = true;

    createTenantForm: FormGroup<{
        details: FormGroup<{
            name: FormControl<string>,
            description: FormControl<string>,
        }>,
        userConfig: FormGroup<{
            defaultAdminName: FormControl<string>,
            defaultAdminPassword: FormControl<string>,
            repeatDefaultAdminPassword: FormControl<string>,
        }>
    }>;


    tenantsPage: PaginatedResponse<TenantResponse> = {
        pageSize: 0,
        content: [],
        totalPages: 0,
    } as unknown as PaginatedResponse<TenantResponse>;

    private restQuery: QueryRequest = {
        page: 1,
        pageSize: 5,
    };

    constructor(
        private tenantService: TenantService,
        private fb: FormBuilder,
    ) {
    }

    ngOnInit(): void {
        this.subscribeToTenantsGrid();
        this.buildForm();
    }

    buildForm(): void {
        this.createTenantForm = this.fb.group({
            details: this.fb.group({
                name: ["", Validators.required],
                description: ["", Validators.required],
            }),
            userConfig: this.fb.group({
                defaultAdminName: ["", Validators.required],
                defaultAdminPassword: ["", [Validators.required, Validators.minLength(6)]],
                repeatDefaultAdminPassword: ["", Validators.required],
            }, {
                validators: [FormValidators.matchPasswords(
                    "defaultAdminPassword",
                    "repeatDefaultAdminPassword"
                )]
            } as AbstractControlOptions),
        });
    }

    public subscribeToTenantsGrid(): void {
        this.onDataGridRefresh.pipe(
            debounceTime(500),
            mergeMap((state) => {
                this.loading = true;
                this.restQuery = {
                    pageSize: state?.page?.size || 5,
                    page: state.page?.current || 1,
                    sort: state.sort ? {
                        sortField: state.sort.by as string,
                        sortType: state.sort.reverse ? QueryRequestSortType.DESC : QueryRequestSortType.ASC
                    } : undefined,
                    filter: buildRestGridFilter(state.filters)
                }
                return this.tenantService.getTenants(this.restQuery);
            })).subscribe({
            next: (response) => {
                this.tenantsPage = response;
                this.loading = false;

            }, error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
            }
        });
    }

    openCreateTenantModal(): void {
        this.createTenantModalOpened = true;
        this.createTenantForm.controls.details.reset({
            name: "",
            description: "",
        });

        this.createTenantForm.controls.userConfig.reset({
            defaultAdminName: "",
            defaultAdminPassword: "",
        });
    }

    createTenant(): void {
        if (this.createTenantForm.invalid) {
            return;
        }
        this.creatingTenantLoading = true;

        this.tenantService.createTenant({
            name: this.createTenantForm.controls.details.controls.name.value,
            description: this.createTenantForm.controls.details.controls.description.value,
            defaultAdminName: this.createTenantForm.controls.userConfig.controls.defaultAdminName.value,
            defaultAdminPassword: this.createTenantForm.controls.userConfig.controls.defaultAdminPassword.value,
        } as CreateTenantRequest).subscribe({
            next: (tenant) => {
                this.creatingTenantLoading = false;
                this.refresh();
                this.createTenantModalOpened = false;
            },
            error: (error) => {
                this.createTenantError = resolveErrorMessage(error);
                this.alertErrorCreateTenantClosed = false;
                this.creatingTenantLoading = false;
            }
        })
    }

    public refreshByGrid(state: ClrDatagridStateInterface): void {
        this.onDataGridRefresh.next(state);
    }

    public refresh(): void {
        this.tenantService.getTenants(this.restQuery).subscribe((response) => {
            this.tenantsPage = response;
        });
    }
}
