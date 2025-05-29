import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params, Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {catchError, map, mergeMap, Observable, tap, throwError} from 'rxjs';
import {resolveErrorMessage} from '../../common/utils/util-functions';
import {AuthService} from '../../services/auth.service';
import {UserChatRightsResponse, UserResponse} from '../../common/rest/types/responses/user-response';
import {MULTIKUBE_ROUTE_PATHS} from '../../app.routes';
import {ClrAlertModule, ClrSpinnerModule} from '@clr/angular';

@Component({
    selector: 'app-chat-home',
    imports: [
        RouterOutlet,
        ClrAlertModule,
        ClrSpinnerModule
    ],
    templateUrl: './chat-home.component.html',
    standalone: true,
    styleUrl: './chat-home.component.scss'
})
export class ChatHomeComponent implements OnInit {

    loading = false;
    errorMessage = "";
    alertClosed = true;
    currentUser: UserChatRightsResponse;
    currentUserId: number;
    currentChatId: number;

    constructor(
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private authService: AuthService,
    ) {
        this.currentUserId = authService.getUserIdentity().id;
    }

    ngOnInit(): void {
        this.initChatHomeComponent();
    }

    public initChatHomeComponent(): void {
        this.loading = true;
        (this.activatedRoute.params as Observable<Params>).pipe(
            map((routeParameters) => {
                return routeParameters[MULTIKUBE_ROUTE_PATHS.KUBE_ID];
            })
        ).pipe(
            tap((currentUserWithRights: UserChatRightsResponse) => {
                this.currentUser = currentUserWithRights;
                //return this.loadCorrespondence(this.currentChatId);
            })).subscribe({
            next: () => {
            }
        });
    }


    protected readonly CHAT_ROUTE_PATHS = MULTIKUBE_ROUTE_PATHS;
}
