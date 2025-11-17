// TypeScript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { jwtDecode } from 'jwt-decode';
import { Router } from '@angular/router';
import { BASE_URL } from '../constants/constants';
import { User } from './user.service';

interface AuthResponse { token?: string; message?: string; avatar?: string; }
interface DecodedToken { sub?: string; userId?: string; id?: string; role: string; exp: number; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${BASE_URL}/user-service/api/auth`;
  private decodedToken: DecodedToken | null = null;
  private avatarUrl: string | null = null;

  constructor(private http: HttpClient, private router: Router) {
    const token = localStorage.getItem('token');
    if (token) this.safeDecode(token);
  }

  private safeDecode(token: string) {
    try { this.decodedToken = jwtDecode<DecodedToken>(token); }
    catch { this.decodedToken = null; }
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUserId(): string | null {
    return this.decodedToken?.userId || this.decodedToken?.id || this.decodedToken?.sub || null;
  }

  setAvatar(url: string) {
    this.avatarUrl = url;
  }

  getAvatar(): string | null {
    return this.avatarUrl;
  }

  isLoggedIn(): boolean {
    return !!this.decodedToken && this.decodedToken.exp * 1000 > Date.now();
  }

  getUserRole(): string | null {
    return this.decodedToken ? this.decodedToken.role : null;
  }

  signup(data: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, data);
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem('token', res.token);
          this.safeDecode(res.token);

        }
        if (res.avatar) {
          this.setAvatar(res.avatar);  // store avatar in the service
        }
      this.router.navigate(['/']).then(() => window.location.reload());
      })
    );
  }

  loginNoReload(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem('token', res.token);
          this.safeDecode(res.token);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.decodedToken = null;
    window.location.reload();
  }
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${BASE_URL}/user-service/api/users/me`);
  }
}
