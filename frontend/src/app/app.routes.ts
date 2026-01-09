import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { SellerProfileComponent } from './components/sellerProfile/sellerProfile.component';
import { ClientProfileComponent } from './components/clientProfile/clientProfile.component';
import { AuthGuard } from './guards/auth.guard';
import { RoleGuard } from './guards/role.guard';
import { ProductViewComponent } from './components/product-view/product-view.component';
import { ManageProductsComponent } from './components/manage-products/manage-products.component';
import { OrderViewComponent } from './components/order-view/order-view.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { productOwnerGuard } from './guards/product-owner.guard';
import { SessionGuard } from './guards/session.guard';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'auth', component: AuthComponent, canActivate: [SessionGuard] },
  { path: 'products/manage', component: ManageProductsComponent, canActivate: [AuthGuard]  },
  { path: 'products/update/:id', component: ManageProductsComponent, canActivate: [productOwnerGuard] },
  { path: 'products/:id', component: ProductViewComponent },
  { path: 'seller-profile', component: SellerProfileComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'SELLER' }  },
  { path: 'client-profile', component: ClientProfileComponent, canActivate: [AuthGuard, RoleGuard], data: { role: 'CLIENT' }  },
  { path: 'order/:id', component: OrderViewComponent, canActivate: [AuthGuard]},
  { path: '404', component: NotFoundComponent },
  { path: '**', redirectTo: '/404' }
];
