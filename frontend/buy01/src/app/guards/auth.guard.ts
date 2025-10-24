import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn?.()) {
      return true; // User is authenticated
    } else {
      this.router.navigate(['/auth']); // Redirect to login if not authenticated
      return false;
    }
  }
}
