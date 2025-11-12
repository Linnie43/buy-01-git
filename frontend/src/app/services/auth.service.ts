import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode';
import { Router } from '@angular/router';
import { BASE_URL } from '../constants/constants';
import { AppComponent } from '../app.component';


interface AuthResponse {
  token?: string;
  message?: string;
}

interface DecodedToken {
  sub: string; // The user's unique ID
  userId?: string;
  id?: string;
  role: string;
  iat: number;
  exp: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${BASE_URL}/user-service/api/auth`;
  private decodedToken: DecodedToken | null = null;
  private token: string | null = null;
  public appComponent!: AppComponent;

  constructor(
    private http: HttpClient,
    private router: Router
    ) {

     const token = localStorage.getItem('token');
         if (token) {
           try {
             this.decodedToken = jwtDecode<DecodedToken>(token);
           } catch (error) {
             console.error('Error decoding token on startup:', error);
             this.logout();
           }
         }
     }

  isLoggedIn(): boolean {
      // Check if token exists and is not expired
      return !!this.decodedToken && this.decodedToken.exp * 1000 > Date.now();
    }

  getUserRole(): string | null {
      return this.decodedToken ? this.decodedToken.role : null;
    }

  // synchronous getter for interceptor and other callers
  getToken(): string | null {
      return this.token ?? localStorage.getItem('token');
    }

  signup(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, userData);
    }

login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem('token', res.token);
          try {
            this.decodedToken = jwtDecode<DecodedToken>(res.token);
            this.router.navigate(['/dashboard']).then(() => {
              window.location.reload();
            });
            } catch (error) {
              console.error('Error decoding token on login:', error);}
              this.decodedToken = null;

        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.decodedToken = null;
    this.router.navigate(['/']).then(() => {
      window.location.reload();
  });
  }

}

