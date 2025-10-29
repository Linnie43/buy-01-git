import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Router } from '@angular/router';


interface AuthResponse {
  token?: string;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'https://localhost:8443/auth';
  private currentUser: { role: string } | null = null;

  constructor(private http: HttpClient, private router: Router) {

    const userData = localStorage.getItem('user');
    this.currentUser = userData ? JSON.parse(userData) : null;

  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem('token');
    return !!token;
  }

  getUserRole(): string | null {
    return this.currentUser?.role ?? null;
  }

  signup(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, userData);
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem('token', res.token);
          // optionally store user role if returned by backend
          this.currentUser = { role: 'client' }; // replace with actual role from backend if available
          localStorage.setItem('user', JSON.stringify(this.currentUser));
        }
      })
    );
  }

  logout() {
    this.currentUser = null;
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    this.router.navigate(['/']).then(() => {
      window.location.reload();
  });
  }

  // testing helpers
  loginAsClient() {
    this.currentUser = { role: 'client' };
    localStorage.setItem('user', JSON.stringify(this.currentUser));
  }

  loginAsSeller() {
    this.currentUser = { role: 'seller' };
    localStorage.setItem('user', JSON.stringify(this.currentUser));
  }

 getCurrentUserId(): string | null {
   const user = localStorage.getItem('user');
   if (!user) return null;
   return JSON.parse(user).id;
 }

}

