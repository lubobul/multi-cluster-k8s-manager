import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {AuthService} from '../../services/auth.service';
import {Router, RouterLink} from '@angular/router';
import {ClarityModule} from '@clr/angular';
import {resolveErrorMessage} from '../../common/utils/util-functions';
import {JwtResponse} from '../../common/rest/types/auth-types';
import {JwtHelperService} from '@auth0/angular-jwt';

@Component({
    selector: 'app-chat-login',
    imports: [
        ClarityModule,
        ReactiveFormsModule,
        RouterLink
    ],
    templateUrl: './chat-login.component.html',
    standalone: true,
    styleUrl: './chat-login.component.scss'
})
export class ChatLoginComponent {
    private readonly TOKEN_KEY = "jwt";
    private readonly USER_ID_KEY = "userId";
    loginForm: FormGroup;
    errorMessage: string | null = null;
    alertClosed = true;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private jwtHelper: JwtHelperService
    ) {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]],
        });
    }

    onSubmit(): void {
        if (this.loginForm.invalid) {
            return;
        }

        const { email, password } = this.loginForm.value;

        this.authService.login({ email, password }).subscribe({
            next: (jwtResp: JwtResponse) => {
                this.storeUserIdentity(jwtResp);
                this.router.navigate(['/home']); // Navigate to the home page after login
            },
            error: (error) => {
                this.alertClosed = false;
                this.errorMessage = resolveErrorMessage(error);
                console.error(error);
            },
        });
    }

    private storeUserIdentity(jwtResponse: JwtResponse): void {
        localStorage.setItem(this.TOKEN_KEY, jwtResponse.token);
        const decodedToken = this.jwtHelper.decodeToken(jwtResponse.token);
        localStorage.setItem(this.USER_ID_KEY, decodedToken?.userId);
    }

}
