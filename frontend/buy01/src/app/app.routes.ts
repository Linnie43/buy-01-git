import { Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { SignupComponent } from './components/auth/signup/signup.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SellerProfileComponent } from './components/sellerProfile/sellerProfile.component';
import { ClientProfileComponent } from './components/clientProfile/clientProfile.component';
//import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'seller-profile', component: SellerProfileComponent, canActivate: [RoleGuard] },
  { path: 'client-profile', component: ClientProfileComponent, canActivate: [RoleGuard] },
  { path: '**', redirectTo: '/login' }
];
