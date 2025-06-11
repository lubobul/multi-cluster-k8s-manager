import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {ClusterResponse, ClusterStatus} from '../rest/types/provider/responses/ClusterResponse';
import {CdsModule} from '@cds/angular';
import {ClrSpinnerModule} from '@clr/angular';

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
    cluster?: ClusterResponse;

    get status(): ClusterStatus {
        if (!this.cluster){
            return ClusterStatus.UNREACHABLE;
        }

        return this.cluster.status;
    }

    protected readonly ClusterStatus = ClusterStatus;
}
