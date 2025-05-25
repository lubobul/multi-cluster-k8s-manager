import {AfterViewChecked, Component, OnInit} from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ClrInputModule, ClrPasswordModule} from '@clr/angular';
import {ThemeService} from './common/services/theme.service';

@Component({
    selector: 'app-root',
    imports: [
        RouterOutlet,
        FormsModule,
        ClrInputModule,
        ReactiveFormsModule,
        ClrPasswordModule,
    ],
    templateUrl: './app.component.html',
    standalone: true,
    styleUrl: './app.component.scss',
})
export class AppComponent {
    constructor(private themeService: ThemeService) {
        this.themeService.setDefault();
    }
}
