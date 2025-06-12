import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ClusterResponse, ClusterStatus} from '../rest/types/provider/responses/ClusterResponse';
import {CdsModule} from '@cds/angular';
import {ClrSpinnerModule} from '@clr/angular';
import {TenantClusterResponse} from '../rest/types/tenant/responses/TenantClusterResponse';

@Component({
    selector: 'cluster-status',
    imports: [
        CdsModule,
        ClrSpinnerModule
    ],
    templateUrl: './cluster-status.component.html',
    styleUrl: './cluster-status.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClusterStatusComponent {

    @Input()
    cluster?: ClusterResponse | TenantClusterResponse;

    get status(): ClusterStatus {
        if (!this.cluster){
            return ClusterStatus.UNREACHABLE;
        }

        return this.cluster.status;
    }

    protected readonly ClusterStatus = ClusterStatus;
}
