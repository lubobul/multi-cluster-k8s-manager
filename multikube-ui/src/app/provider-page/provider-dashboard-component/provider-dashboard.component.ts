import { Component } from '@angular/core';
import {UserResponse} from '../../common/rest/types/responses/user-response';
import {AuthService} from '../../services/auth.service';

@Component({
    selector: 'app-welcome-screen',
    imports: [],
    templateUrl: './provider-dashboard.component.html',
    standalone: true,
    styleUrl: './provider-dashboard.component.scss'
})
export class ProviderDashboardComponent {

    constructor(private authService: AuthService) {
    }

    public get userIdentity(): UserResponse{
        return this.authService.getUserIdentity();
    }
}
