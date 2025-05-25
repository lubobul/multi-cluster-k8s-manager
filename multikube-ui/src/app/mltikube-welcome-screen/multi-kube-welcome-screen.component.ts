import { Component } from '@angular/core';
import {UserResponse} from '../common/rest/types/responses/user-response';
import {AuthService} from '../services/auth.service';

@Component({
    selector: 'app-welcome-screen',
    imports: [],
    templateUrl: './multi-kube-welcome-screen.component.html',
    standalone: true,
    styleUrl: './multi-kube-welcome-screen.component.scss'
})
export class MultiKubeWelcomeScreenComponent {

    constructor(private authService: AuthService) {
    }

    public get userIdentity(): UserResponse{
        return this.authService.getUserIdentity();
    }
}
