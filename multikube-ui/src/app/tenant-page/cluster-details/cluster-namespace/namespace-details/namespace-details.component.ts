import {Component, OnInit} from '@angular/core';
import {TenantNamespaceResponse} from '../../../../common/rest/types/tenant/responses/TenantNamespace';
import {TenantNamespaceService} from '../../../services/tenant-namespace.service';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {map, mergeMap, Observable} from 'rxjs';
import {TENANT_ROUTE_PATHS} from '../../../../app.routes';
import {resolveErrorMessage} from '../../../../common/utils/util-functions';
import {ClrAlertModule, ClrSpinnerModule} from '@clr/angular';
import {NamespaceStatusComponent} from '../../../../common/namespace-status/namespace-status.component';
import {DatePipe} from '@angular/common';
import {TenantNamespaceDetailsService} from '../../../services/tenant-namespace-details.service';

@Component({
  selector: 'app-namespace-details',
    imports: [
        ClrAlertModule,
        ClrSpinnerModule,
        NamespaceStatusComponent,
        DatePipe
    ],
  templateUrl: './namespace-details.component.html',
  styleUrl: './namespace-details.component.scss'
})
export class NamespaceDetailsComponent implements OnInit {
    loading = false;
    errorMessage = "";
    alertErrorClosed = true;
    namespace: TenantNamespaceResponse;

    constructor(
        private namespaceService: TenantNamespaceService,
        private namespaceDetailsService: TenantNamespaceDetailsService,
        private activatedRoute: ActivatedRoute,
        private router: Router,
    ) {
    }

    ngOnInit(): void {
        this.loading = true;
        this.namespaceDetailsService.namespace$.subscribe({
            next: (namespace) => {
                this.namespace = namespace;
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = resolveErrorMessage(err);
                this.alertErrorClosed = false;
                this.loading = false;
            }
        });
    }
}
