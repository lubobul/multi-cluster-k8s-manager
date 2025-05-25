// theme.service.ts
import {Injectable} from '@angular/core';
import {Inject, PLATFORM_ID} from '@angular/core';
import {isPlatformBrowser} from '@angular/common';

@Injectable({
    providedIn: 'root',
})
export class ThemeService {

    constructor(
        @Inject(PLATFORM_ID) private platformId: Object
    ) {
    }

    // Get the current theme from localStorage or default to 'light'
    getCurrentTheme(): string {
        if (isPlatformBrowser(this.platformId)) {
            return localStorage.getItem('darkMode') === 'true' ? 'dark' : 'light';
        }
        return 'light'; // Fallback if not running in the browser
    }

    // Set the theme on the <body> element and save to localStorage
    setTheme(isDark: boolean): void {
        const theme = isDark ? 'dark' : 'light';
        if (isPlatformBrowser(this.platformId)) {
            document.body.setAttribute('cds-theme', theme);
            localStorage.setItem('darkMode', isDark ? 'true' : 'false');
        }
    }

    setDefault(): void{
        const theme = this.getCurrentTheme();
        document.body.setAttribute('cds-theme', theme);
    }
}
