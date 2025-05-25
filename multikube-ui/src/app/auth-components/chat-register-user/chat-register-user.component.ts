import { Component } from '@angular/core';
import {ClrAlertModule, ClrCommonFormsModule, ClrInputModule, ClrPasswordModule} from '@clr/angular';
import {
    AbstractControlOptions,
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators
} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {resolveErrorMessage} from '../../common/utils/util-functions';
import {FormValidators} from '../../common/utils/form-validators';

@Component({
    selector: 'app-chat-register-user',
    imports: [
        ClrAlertModule,
        ClrCommonFormsModule,
        ClrInputModule,
        ClrPasswordModule,
        FormsModule,
        ReactiveFormsModule,
    ],
    templateUrl: './chat-register-user.component.html',
    standalone: true,
    styleUrl: './chat-register-user.component.scss'
})
export class ChatRegisterUserComponent {
    registerForm: FormGroup;
    errorMessage: string | null = null;
    alertClosed = true;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.registerForm = this.fb.group({
            username: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            repeatPassword: ['', [Validators.required, Validators.minLength(6)]],
        }, {
            validators: [FormValidators.matchPasswords(
                "password",
                "repeatPassword"
            )]
        } as AbstractControlOptions);
    }

    onSubmit(): void {
        if (this.registerForm.invalid) {
            return;
        }

        const { username, email, password } = this.registerForm.value;

        this.authService.register({ username, email, password }).subscribe({
            next: () => {
                this.router.navigate(['/login']); // Navigate to the home page after login
            },
            error: (error) => {
                this.errorMessage = resolveErrorMessage(error);
                this.alertClosed = false;
            },
        });
    }
}
