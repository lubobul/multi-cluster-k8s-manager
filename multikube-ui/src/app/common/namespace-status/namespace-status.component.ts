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

@Component({
    selector: 'namespace-status',
    imports: [
        CdsModule,
        ClrSpinnerModule,
        ClarityModule
    ],
    templateUrl: './namespace-status.component.html',
    styleUrl: './namespace-status.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NamespaceStatusComponent {

    @Input()
    namespace?: TenantNamespaceResponse | TenantNamespaceSummaryResponse;

    get status(): NamespaceStatus {
        if (!this.namespace){
            return NamespaceStatus.UNKNOWN;
        }

        return this.namespace.status;
    }

    protected readonly NamespaceStatus = NamespaceStatus;
    protected readonly File = File;
}
