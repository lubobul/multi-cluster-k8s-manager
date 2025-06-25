import {Component, OnInit} from '@angular/core';
import {
    ClarityModule,
    ClrAccordionModule,
    ClrCommonFormsModule,
    ClrInputModule,
    ClrStepperModule,
    ClrTextareaModule
} from '@clr/angular';
import {EditorComponent} from 'ngx-monaco-editor-v2';
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {TenantClusterDetailsService} from '../../services/tenant-cluster-details.service';
import {FormValidators} from '../../../common/utils/form-validators';
import {TenantNamespaceService} from '../../services/tenant-namespace.service';
import {CreateNamespaceRequest} from '../../../common/rest/types/tenant/requests/CreateNamespaceRequest';
import {resolveErrorMessage} from '../../../common/utils/util-functions';
import {TenantClusterResponse} from '../../../common/rest/types/tenant/responses/TenantClusterResponse';
import {TENANT_ROUTE_PATHS} from '../../../app.routes';

export enum NamespaceConfigurationChoice {
    Default = "Default",
    Custom = "Custom",
}

@Component({
    selector: 'cluster-namespaces',
    imports: [
        ClarityModule,
        ClrAccordionModule,
        ClrCommonFormsModule,
        ClrInputModule,
        ClrStepperModule,
        ClrTextareaModule,
        FormsModule,
        ReactiveFormsModule,
        EditorComponent
    ],
    templateUrl: './create-cluster-namespace.component.html',
    styleUrl: './create-cluster-namespace.component.scss'
})
export class CreateClusterNamespaceComponent implements OnInit {
    loading = false;
    errorMessage = "";
    alertErrorClosed = true;
    cluster: TenantClusterResponse;

    protected readonly NamespaceConfigurationChoice = NamespaceConfigurationChoice;

    namespaceForm: FormGroup<{
        details: FormGroup<{
            name: FormControl<string>,
            description: FormControl<string>,
        }>,
        resourceQuota: FormGroup<{
            choice: FormControl<NamespaceConfigurationChoice>,
            resourceQuotaYaml: FormControl<string>,
        }>,
        limitRange: FormGroup<{
            choice: FormControl<NamespaceConfigurationChoice>,
            limitRangeYaml: FormControl<string>,
        }>,
    }>;

    public editorOptions = {theme: "vs", language: "yaml", automaticLayout: true, minimap: {enabled: false}};

    private readonly resourceQuotaExample = `apiVersion: v1
kind: ResourceQuota
metadata:
  name: tenant-a-quota
  namespace: my-new-namespace
spec:
  hard:
    requests.cpu: "2000m"
    limits.cpu: "4000m"
    requests.memory: 2Gi
    limits.memory: 4Gi
    requests.storage: 50Gi
    persistentvolumeclaims: "10"
    pods: "20"`;

    private readonly limitRangeExample = `apiVersion: v1
kind: LimitRange
metadata:
  name: default-resource-limits
  namespace: my-new-namespace
spec:
  limits:
    - type: "Container"
      # --- Default resource requests for new containers ---
      # If a container is created without specifying resources, it gets these
      defaultRequest:
        cpu: "250m"
        memory: "256Mi"
      # --- Default resource limits for new containers ---
      # Every container will have this limit applied unless it specifies its own
      default:
        cpu: "500m"
        memory: "512Mi"
      # --- Maximum allowed resources for any single container ---
      # A container cannot request or be limited to more than this
      max:
        cpu: "1000m"  # 1 core
        memory: "1Gi"`;

    constructor(
        private clusterDetailsService: TenantClusterDetailsService,
        private namespaceService: TenantNamespaceService,
        private fb: FormBuilder,
        private router: Router,
        private activatedRoute: ActivatedRoute,
    ) {
    }

    ngOnInit(): void {
        this.buildForm();
        this.loading = true;
        this.clusterDetailsService.cluster$.subscribe({
            next: (cluster) => {
                this.cluster = cluster;
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertErrorClosed = false;
                this.loading = false;
            }
        });
    }

    buildForm(): void {
        this.namespaceForm = this.fb.group({
            details: this.fb.group({
                name: ["", [Validators.required, FormValidators.dnsCompliantValidator()]],
                description: [""],
            }),
            resourceQuota: this.fb.group({
                choice: [NamespaceConfigurationChoice.Default, Validators.required],
                resourceQuotaYaml: [this.resourceQuotaExample],
            }),
            limitRange: this.fb.group({
                choice: [NamespaceConfigurationChoice.Default, Validators.required],
                limitRangeYaml: [this.limitRangeExample],
            })
        });
    }

    public createNamespace(): void{
        this.loading = true;
        this.namespaceService.createNamespace({
            clusterId: this.cluster.id,
            name: this.namespaceForm.controls.details.controls.name.value,
            description: this.namespaceForm.controls.details.controls.description.value,
            resourceQuotaYaml: this.namespaceForm.controls.resourceQuota.controls.choice.value === NamespaceConfigurationChoice.Custom ?
                this.namespaceForm.controls.resourceQuota.controls.resourceQuotaYaml.value : undefined,
            limitRangeYaml: this.namespaceForm.controls.limitRange.controls.choice.value === NamespaceConfigurationChoice.Custom ?
                this.namespaceForm.controls.limitRange.controls.limitRangeYaml.value : undefined,
        } as CreateNamespaceRequest).subscribe({
            next: (namespace) => {
                this.loading = false;
                this.clusterDetailsService.updateClusterDetails();
                this.navigateToNamespaceDetails(namespace.id);
            },
            error: (err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertErrorClosed = false;
                this.loading = false;
            }
        });
    }

    private navigateToNamespaceDetails(namespaceId: number): void {
        this.router.navigate(
            [`../${TENANT_ROUTE_PATHS.CLUSTER_DETAILS_CHILDREN.NAMESPACES}/${namespaceId}`,],
            {
                relativeTo: this.activatedRoute,
            }
        );
    }
}
