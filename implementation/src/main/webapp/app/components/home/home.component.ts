import { Component, OnInit, OnDestroy, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { AccountService } from 'app/services/auth/account.service';
import { Account } from 'app/services/auth/account.model';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy {
  account: Account | null = null;
  
  private readonly destroy$ = new Subject<void>();

  constructor(private accountService: AccountService, private router: Router, private elRef: ElementRef) {
    document.addEventListener("collapse", () => {
      this.elRef.nativeElement.querySelector(".logo-wrapper")
      .classList.add("collapsed");
    });
  }

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => (this.account = account));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  login(): void {
    this.router.navigate(['/login']);
  }
  
}