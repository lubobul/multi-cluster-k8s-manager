import {Pipe, PipeTransform} from '@angular/core';
import {ClusterResponse} from '../../../common/rest/types/provider/responses/ClusterResponse';
import {TenantResponse} from '../../../common/rest/types/provider/responses/TenantResponse';

@Pipe({
    name: 'isClusterPublished'
})
export class IsClusterPublishedPipe implements PipeTransform {

    transform(cluster: ClusterResponse, tenant: TenantResponse): boolean {
        return tenant?.allocatedClusterIds?.some(clusterId => clusterId === cluster.id);
    }

}
