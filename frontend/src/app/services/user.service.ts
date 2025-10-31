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

  private apiUrl = `${BASE_URL}/users/me`; // endpoint

  constructor(private http: HttpClient) {}

  getMe(): Observable<User> {
    return this.http.get<User>(this.apiUrl);
  }
}
