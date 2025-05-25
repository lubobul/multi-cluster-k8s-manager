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
export class ProfileSettingsComponent implements OnInit{
    updateProfileForm: FormGroup = {} as any;
    errorMessage: string | null = null;
    alertClosed = true;
    user: UserResponse = {} as any;
    showConfirm = false;
    darkMode: boolean;
    avatar64?: string | ArrayBuffer | null;
    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private themeService: ThemeService
    ) {
        this.darkMode = this.themeService.getCurrentTheme() === 'dark';
    }

    ngOnInit(): void {
        this.buildForm(this.authService.getUserIdentity());

    }

    //TODO Could potentially be checking if user exists while typing username
    private buildForm(user: UserResponse): void{
        this.avatar64 = user.avatar;
        this.updateProfileForm = this.fb.group({
            username: [user.username, [Validators.required, Validators.minLength(3)]],
            email: [user.email, [Validators.required, Validators.email]],
        } as AbstractControlOptions);

        this.updateProfileForm.get("email")?.disable();
    }

    updateProfile(): void{
        this.authService.updateProfile(
            {
                username: this.updateProfileForm.get("username")?.value,
                avatar: this.avatar64,
            } as UpdateProfileRequest
        ).subscribe({
            next: (user: UserResponse) => {
                this.router.navigate([MULTIKUBE_ROUTE_PATHS.HOME]);
            },
            error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
            },
        })
    }

    logout(): void{
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


    openDeleteProfileModal(): void{
        this.showConfirm = true;
    }

    deleteProfile(): void{
        this.authService.deleteProfile().subscribe({
            next: () => {
                this.router.navigate([MULTIKUBE_ROUTE_PATHS.LOGIN]);
            },
            error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
            },
        })
    }

    toggleDarkMode(event: any): void{
        this.themeService.setTheme(event.currentTarget.checked);
    }

    public selectAvatar(): void {
        const input = document.createElement("input");
        input.type = "file";
        input.accept = ".png,.jpeg,.jpg";
        input.onchange = (event: any) => {
            const file = event.target.files[0];

            this.toBase64(file).then((avatar64) => {
                this.avatar64 = avatar64;
            }).catch((err) => {
                console.error(err);
            });
        };
        input.click();
    }

    private toBase64(file: any): Promise<string | ArrayBuffer | null> {
        return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.readAsDataURL(file);
                reader.onload = () => resolve(reader.result);
                reader.onerror = reject;
            }
        );
    }
}
