import { Component } from '@angular/core';
import {ClarityModule} from '@clr/angular';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {MULTIKUBE_ROUTE_PATHS} from '../../app.routes';
import {ClusterDetailsService} from '../services/cluster-details.service';

@Component({
  selector: 'app-cluster-details',
    imports: [
        ClarityModule,
        RouterOutlet,
        RouterLink,
        RouterLinkActive
    ],
    providers: [
        ClusterDetailsService
    ],
  templateUrl: './cluster-details.component.html',
  styleUrl: './cluster-details.component.scss'
})
export class ClusterDetailsComponent {

    protected readonly MULTIKUBE_ROUTE_PATHS = MULTIKUBE_ROUTE_PATHS;
}
