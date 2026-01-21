import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private readonly snackBar: MatSnackBar,
    private readonly authService: AuthService
    ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    let authReq = req;

    // Exclude signup and any public endpoints
    const excludedUrls = ['/api/auth/signup', '/api/auth/login'];
    const isExcluded = excludedUrls.some(url => req.url.includes(url));

    if (!isExcluded && token && this.authService.isLoggedIn()) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('AuthInterceptor - Modified request with Authorization header');
    } else {
      console.log('AuthInterceptor - No token attached (excluded URL or no token)');
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMsg = 'Something went wrong';
        const errorBody = error.error;

        const getSpringErrorMessage = (body: any): string | null => {
            if (body && typeof body === 'object') {
              const values = Object.values(body);
              return values.length > 0 ? String(values[0]) : null;
            }
            return null;
          };

        const serverSpecificMsg = getSpringErrorMessage(errorBody);

        if (error.status === 401 && !req.url.includes('/api/auth/login')) {
            errorMsg = 'Please log in to continue.';
            this.authService.logout();
          } else if (error.status === 404) {
            errorMsg = serverSpecificMsg || 'Resource not found';
          } else if (error.status === 403) {
            errorMsg = serverSpecificMsg || 'Access Denied: You do not have permission.';
          } else if (error.status === 0) {
            errorMsg = 'Cannot reach server';
          } else if (error.status >= 400 && error.status < 500) {
            errorMsg = serverSpecificMsg || 'Client error';
          } else if (error.status >= 500) {
            errorMsg = 'Oops, try again later';
          }

        this.snackBar.open(errorMsg, 'Close', { duration: 5000 });

        return throwError(() => error);
      })
    );
  }
}
