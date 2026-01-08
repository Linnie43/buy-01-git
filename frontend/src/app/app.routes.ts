import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SellerProfileComponent } from './components/sellerProfile/sellerProfile.component';
import { ClientProfileComponent } from './components/clientProfile/clientProfile.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { ProductViewComponent } from './components/product-view/product-view.component';
import { ManageProductsComponent } from './components/manage-products/manage-products.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { productOwnerGuard } from './guards/product-owner.guard';
import { SessionGuard } from './guards/session.guard';
import { SalesDashboardComponent } from './components/sales-dashboard/sales-dashboard.component';
import { ClientDashboardComponent } from './components/client-dashboard/client-dashboard.component';


export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'auth', component: AuthComponent, canActivate: [SessionGuard] },
  { path: 'products/manage', component: ManageProductsComponent, canActivate: [AuthGuard]  },
  { path: 'products/update/:id', component: ManageProductsComponent, canActivate: [productOwnerGuard] },
  { path: 'products/:id', component: ProductViewComponent },
  { path: 'seller-profile', component: SellerProfileComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' }  },
  { path: 'client-profile', component: ClientProfileComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'CLIENT' }  },
  { path: 'sales-dashboard', component: SalesDashboardComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' } },
  { path: 'my-dashboard', component: ClientDashboardComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'CLIENT' } },
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: '/404' }
];
