import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { ProductService } from '../services/product.service';
import { map, catchError, of } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

export const productOwnerGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const productService = inject(ProductService);
  const router = inject(Router);
  const productId = route.paramMap.get('id');

  if (!productId) {
    // This case should ideally not be reached if routes are set up correctly
    return router.createUrlTree(['/404']);
  }

  return productService.getProductById(productId).pipe(
    map(product => {
      if (product.isProductOwner) {
        return true; // Allow access
      }
      // Not the owner, redirect to 404 page
      return router.createUrlTree(['/404']);
    }),
    catchError((err: unknown) => {
      // If product is not found (404) or access is forbidden (403), redirect
      if (err instanceof HttpErrorResponse && (err.status === 404 || err.status === 403)) {
        return of(router.createUrlTree(['/404']));
      }
      // For any other error, redirect to the home page
      console.error('Error in product owner guard:', err);
      return of(router.createUrlTree(['/']));
    })
  );
};
