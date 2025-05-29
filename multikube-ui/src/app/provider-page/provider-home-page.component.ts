import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router, RouterLink, RouterLinkActive, RouterOutlet} from '@angular/router';
import {
    ClarityModule,
    ClrVerticalNavModule
} from '@clr/angular';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {CdsIconModule} from '@cds/angular';
import {UserResponse} from '../common/rest/types/responses/user-response';
import {
    BehaviorSubject,
    debounceTime, delayWhen,
    distinctUntilChanged,
    filter,
    forkJoin,
    map, mergeMap,
    Observable, of, retry, Subscription,
    switchMap,
    tap, timer
} from 'rxjs';
import {resolveErrorMessage} from '../common/utils/util-functions';
import {MULTIKUBE_ROUTE_PATHS} from '../app.routes';
import {DatePipe} from '@angular/common';
import {AuthService} from '../services/auth.service';

@Component({
    selector: 'application-home',
    imports: [
        RouterOutlet,
        ClrVerticalNavModule,
        RouterLink,
        RouterLinkActive,
        CdsIconModule,
        ClarityModule,
        ReactiveFormsModule,
        DatePipe,
    ],
    templateUrl: './provider-home-page.component.html',
    standalone: true,
    styleUrl: './provider-home-page.component.scss'
})
export class ProviderHomePageComponent implements OnInit, OnDestroy {
    friendSearchControl = new FormControl('');
    chatSearchControl = new FormControl('');
    channelSearchControl = new FormControl('');
    protected readonly CHAT_ROUTE_PATHS = MULTIKUBE_ROUTE_PATHS;
    errorMessage = "";
    alertClosed = true;
    openViewUserModal = false;
    selectedFriend: UserResponse = {} as UserResponse;
    friendActionLoading = false;
    currentUser: UserResponse = {} as UserResponse;

    chatSearch = "";
    channelSearch = "";
    friendsSearch = "";

    loadingFriends = false;
    loadingChats = false;
    loadingChannels = false;

    constructor(
        private authService: AuthService,
        private router: Router,
    ) {
    }

    ngOnInit(): void {
        this.currentUser = this.authService.getUserIdentity();

        this.authService.userProfileUpdate.subscribe((user) =>{
            this.currentUser = user;
        })

        this.refresh(true).subscribe(() => {
            this.startDetailsPolling(5000);
        });
    }

    ngOnDestroy(): void {
        this.stopDetailsPolling();
    }


    public viewUser(user: UserResponse): void {
        this.selectedFriend = user;
        this.openViewUserModal = true;
    }




    public refresh(loadingIndicator?: boolean): Observable<null> {
        return of(null);
    }

    private pollingSbj: BehaviorSubject<number>;
    private pollingSubscription: Subscription;

    private startDetailsPolling(interval: number): void {
        this.stopDetailsPolling();
        this.pollingSbj = new BehaviorSubject<number>(interval);

        this.pollingSubscription = this.pollingSbj.pipe(
            delayWhen((interval) => timer(interval)),
            mergeMap(() => {
                return this.refresh();
            }),
            tap(() => {
                this.pollingSbj.next(interval);
            }),
            retry(),
        ).subscribe();
    }

    private stopDetailsPolling(): void {
        if (this.pollingSbj) {
            this.pollingSbj.complete();
        }

        if (this.pollingSubscription) {
            this.pollingSubscription.unsubscribe();
        }
    }

}
