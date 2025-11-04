import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode';

interface AuthResponse {
  token?: string;
  message?: string;
}

interface DecodedToken {
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8443/user-service/api/auth';

  constructor(private http: HttpClient) {}

  isLoggedIn(): boolean {
    const token = localStorage.getItem('token');
    return !!token;
  }

  getUserRole(): string | null {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decodedToken: DecodedToken = jwtDecode(token);
        return decodedToken.role;
      } catch (error) {
        console.error('Error decoding token:', error);
        return null;
      }
    }
    return null;
  }

  signup(userData: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/signup`, userData);
  }

  login(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem('token', res.token);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
  }
}
