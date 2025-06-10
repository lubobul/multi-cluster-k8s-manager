import { Component } from '@angular/core';
import {ClarityModule} from '@clr/angular';
import {RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {MULTIKUBE_ROUTE_PATHS} from '../../app.routes';

@Component({
  selector: 'app-cluster-details',
    imports: [
        ClarityModule,
        RouterOutlet,
        RouterLink,
        RouterLinkActive
    ],
  templateUrl: './cluster-details.component.html',
  styleUrl: './cluster-details.component.scss'
})
export class ClusterDetailsComponent {

    protected readonly CHAT_ROUTE_PATHS = MULTIKUBE_ROUTE_PATHS;
    protected readonly MULTIKUBE_ROUTE_PATHS = MULTIKUBE_ROUTE_PATHS;
}
