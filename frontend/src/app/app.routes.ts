import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SellerProfileComponent } from './components/sellerProfile/sellerProfile.component';
import { ClientProfileComponent } from './components/clientProfile/clientProfile.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { ProductViewComponent } from './components/product-view/product-view.component';
import { UpdateProductComponent } from './components/update-product/update-product.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'auth', component: AuthComponent },
  { path: 'products/:id', component: ProductViewComponent },
  { path: 'products/update/:id', component: UpdateProductComponent },
  { path: 'seller-profile', component: SellerProfileComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'seller' }  },
  { path: 'client-profile', component: ClientProfileComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'client' }  },
  { path: '**', redirectTo: '/auth' }
];
