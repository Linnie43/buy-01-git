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
    const user = this.authService.currentUserValue;

    if (!user || user.role !== expectedRole) {
      this.router.navigate(['/login']);
      return false;
    }

    return true;
  }
}
