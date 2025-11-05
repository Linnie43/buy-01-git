import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { BASE_URL } from '../constants';

export interface User {
  name: string;
  email: string;
  role: string;
  avatar: string;
  ownProfile: boolean;
}

@Injectable({
  providedIn: 'root'
})

export class UserService {

  private apiUrl = `${BASE_URL}/user-service/api/users/me`; // endpoint

  constructor(private http: HttpClient) {}

    getMe(): Observable<User> {
      // Retrieve the token from localStorage or your AuthService
      const token = localStorage.getItem('token');

      if (!token) {
        // Handle the case where the token is not available
        return of(); // Or throw an error, depending on your error handling strategy
      }

      // Create the headers object with the Authorization header
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'includeCredentials': 'true'
      });

      // Pass the headers in the options object of the GET request
      return this.http.get<User>(this.apiUrl, { headers });
    }
  }
