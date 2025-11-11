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

   // intercept method to add auth token and handle errors
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // get the auth token from local storage
    const token = localStorage.getItem('token');
    console.log('[AuthInterceptor] outgoing', req.method, req.url, 'token present:', !!token);

    // copy the request to add the authorization header
    let authReq = req;
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    console.log('AuthInterceptor - Modified request with Authorization header');
    } else {
      console.log('AuthInterceptor - No token found, sending request without Authorization header');
      }

    // handle errors
      return next.handle(authReq).pipe(
            catchError((error: HttpErrorResponse) => {
              let errorMsg = 'Something went wrong';

              // Only trigger logout for 401/403 on non-login API calls
              if (error.status === 401 && !req.url.includes('/login')) {
                errorMsg = 'Your session has expired. Please log in again.';
                this.authService.logout();
              } else if (error.status === 403) {
                errorMsg = 'Access Denied: You do not have permission to perform this action.';
              } else if (error.status === 0) {
                errorMsg = 'Cannot reach server';
              } else if (error.status >= 400 && error.status < 500) {
                console.log('AuthInterceptor - Client error response', error);
                errorMsg = error.error?.message || 'Client error';
              } else if (error.status >= 500) {
                errorMsg = 'Server error occurred';
              }

        // show the error message in a snackbar
        this.snackBar.open(errorMsg, 'Close', { duration: 5000 });

        return throwError(() => error);
      })
    );
  }
}
