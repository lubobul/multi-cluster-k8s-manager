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
import {ClusterService} from '../../../provider-page/services/cluster.service';
import {ActivatedRoute, Router} from '@angular/router';
import {TenantClusterDetailsService} from '../../services/tenant-cluster-details.service';

export enum NamespaceConfigurationChoice {
    Default = "Default",
    Custom = "Custom",
}

@Component({
    selector: 'app-cluster-namespaces',
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
        private fb: FormBuilder,
        private router: Router,
        private activatedRoute: ActivatedRoute,
    ) {
    }

    ngOnInit(): void {
        this.buildForm();
    }

    buildForm(): void {
        this.namespaceForm = this.fb.group({
            details: this.fb.group({
                name: ["", Validators.required],
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
}
