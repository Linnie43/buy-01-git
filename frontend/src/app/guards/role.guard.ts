import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {

    const expectedRole = route.data['role'];
     if (!this.authService.isLoggedIn?.()) {
          this.router.navigate(['/']);
          return false;
        }

     // for checking the user role
     const userRole = this.authService.getUserRole?.();
        if (userRole !== expectedRole) {
          this.router.navigate(['/']);
          return false;
        }

    return true;
  }
}
