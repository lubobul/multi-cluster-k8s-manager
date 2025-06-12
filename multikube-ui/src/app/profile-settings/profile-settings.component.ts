import {Component, OnInit} from '@angular/core';
import {
    ClarityModule,
    ClrAlertModule,
    ClrCommonFormsModule,
    ClrInputModule,
    ClrModalModule,
    ClrPasswordModule
} from '@clr/angular';
import {
    AbstractControlOptions,
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators
} from '@angular/forms';
import {AuthService} from '../services/auth.service';
import {Router} from '@angular/router';
import {FormValidators} from '../common/utils/form-validators';
import {UsersApiService} from '../common/rest/api-services/users-api.service';
import {resolveErrorMessage} from '../common/utils/util-functions';
import {UserResponse} from '../common/rest/types/responses/user-response';
import {UpdateProfileRequest} from '../common/rest/types/auth-types';
import {MULTIKUBE_ROUTE_PATHS} from '../app.routes';
import {ThemeService} from '../common/services/theme.service';
import {CdsModule} from '@cds/angular';

@Component({
    selector: 'app-profile-settings',
    imports: [
        ClrAlertModule,
        ClrCommonFormsModule,
        ClrInputModule,
        ClrPasswordModule,
        FormsModule,
        ReactiveFormsModule,
        ClrModalModule,
        ClarityModule,
        CdsModule
    ],
    templateUrl: './profile-settings.component.html',
    standalone: true,
    styleUrl: './profile-settings.component.scss'
})
export class ProfileSettingsComponent implements OnInit {
    errorMessage: string | null = null;
    alertClosed = true;
    user: UserResponse = {} as any;
    darkMode: boolean;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private themeService: ThemeService
    ) {
        this.darkMode = this.themeService.getCurrentTheme() === 'dark';
    }

    ngOnInit(): void {
        this.user = this.authService.getUserIdentity();
    }

    logout(): void {
        this.authService.logout().subscribe({
            next: () => {
                this.router.navigate([MULTIKUBE_ROUTE_PATHS.LOGIN]);
            },
            error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
            },
        });
    }

    toggleDarkMode(event: any): void {
        this.themeService.setTheme(event.currentTarget.checked);
    }
}
