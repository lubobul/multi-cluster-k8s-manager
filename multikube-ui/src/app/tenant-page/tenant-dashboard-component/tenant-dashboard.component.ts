import { Component } from '@angular/core';
import {UserResponse} from '../../common/rest/types/responses/user-response';
import {AuthService} from '../../services/auth.service';

@Component({
    selector: 'app-welcome-screen',
    imports: [],
    templateUrl: './tenant-dashboard.component.html',
    standalone: true,
    styleUrl: './tenant-dashboard.component.scss'
})
export class TenantDashboardComponent {

    constructor(private authService: AuthService) {
    }

    public get userIdentity(): UserResponse{
        return this.authService.getUserIdentity();
    }
}
