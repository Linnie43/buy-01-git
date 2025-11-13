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
    private snackBar: MatSnackBar,
    private authService: AuthService
    ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    let authReq = req;

    // Only attach the token if it exists and is not expired.
    if (token && this.authService.isLoggedIn()) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('AuthInterceptor - Modified request with Authorization header');
    } else {
      console.log('AuthInterceptor - No token or token expired, sending request without Authorization header');
    }

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMsg = 'Something went wrong';

        if (error.status === 401) {
          errorMsg = 'Your session has expired. Please log in again.';
          this.authService.logout();
        } else if (error.status === 403) {
          const errorBody = error.error;
          if (errorBody && typeof errorBody.error === 'string' && errorBody.error.includes('Invalid JWT token')) {
             errorMsg = 'Your session is invalid. Please log in again.';
             this.authService.logout();
          } else {
             errorMsg = 'Access Denied: You do not have permission to perform this action.';
          }
        } else if (error.status === 0) {
          errorMsg = 'Cannot reach server';
        } else if (error.status >= 400 && error.status < 500) {
          console.log('AuthInterceptor - Client error response', error);
          errorMsg = error.error?.message || 'Client error';
        } else if (error.status >= 500) {
          errorMsg = 'Server error occurred';
        }

        this.snackBar.open(errorMsg, 'Close', { duration: 5000 });

        return throwError(() => error);
      })
    );
  }
}
