import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ClusterResponse, ClusterStatus} from '../rest/types/provider/responses/ClusterResponse';
import {CdsModule} from '@cds/angular';
import {ClarityModule, ClrSpinnerModule} from '@clr/angular';
import {TenantClusterResponse} from '../rest/types/tenant/responses/TenantClusterResponse';
import {
    NamespaceStatus,
    TenantNamespaceResponse,
    TenantNamespaceSummaryResponse
} from '../rest/types/tenant/responses/TenantNamespace';
import {K8sResource, ResourceStatus, SyncStatus} from '../rest/types/tenant/responses/TenantNamespaceResources';

@Component({
    selector: 'k8s-resource-sync-status',
    imports: [
        CdsModule,
        ClrSpinnerModule,
        ClarityModule
    ],
    templateUrl: './k8s-resource-sync-status.component.html',
    styleUrl: './k8s-resource-sync-status.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class k8sResourceSyncStatusComponent {

    @Input()
    k8sResource?: K8sResource;

    get status(): SyncStatus {
        if (!this.k8sResource) {
            return SyncStatus.UNKNOWN;
        }

        return this.k8sResource.syncStatus;
    }

    protected readonly SyncStatus = SyncStatus;
}
