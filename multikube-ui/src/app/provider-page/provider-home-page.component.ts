import {Component, OnInit} from '@angular/core';
import {Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {
    ClarityModule,
    ClrVerticalNavModule
} from '@clr/angular';
import {ReactiveFormsModule} from '@angular/forms';
import {CdsIconModule} from '@cds/angular';
import {UserResponse} from '../common/rest/types/responses/user-response';

import {MULTIKUBE_ROUTE_PATHS} from '../app.routes';
import {AuthService} from '../services/auth.service';

@Component({
    selector: 'application-home',
    imports: [
        RouterOutlet,
        ClrVerticalNavModule,
        RouterLink,
        RouterLinkActive,
        CdsIconModule,
        ClarityModule,
        ReactiveFormsModule,
    ],
    templateUrl: './provider-home-page.component.html',
    standalone: true,
    styleUrl: './provider-home-page.component.scss'
})
export class ProviderHomePageComponent implements OnInit {

    protected readonly CHAT_ROUTE_PATHS = MULTIKUBE_ROUTE_PATHS;
    currentUser: UserResponse = {} as UserResponse;

    constructor(
        private authService: AuthService,
    ) {
    }

    ngOnInit(): void {
        this.currentUser = this.authService.getUserIdentity();

        this.authService.userProfileUpdate.subscribe((user) =>{
            this.currentUser = user;
        })
    }
}
