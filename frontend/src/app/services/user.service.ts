import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { USER_BASE_URL } from '../constants/constants';
import { Product } from '../models/product.model';
import { WINDOW } from '../window.token';

export interface User {
  name: string;
  email: string;
  role: string;
  avatar: string;
  ownProfile: boolean;
  products: Product[];
}

@Injectable({
  providedIn: 'root'
})

export class UserService {

  private window = inject(WINDOW);
  private apiUrl = `${USER_BASE_URL}/me`; // endpoint

  constructor(private http: HttpClient) {}

    getMe(): Observable<User> {
      return this.http.get<User>(this.apiUrl);
    }
    putMe(): Observable<User> {
      return this.http.put<User>(this.apiUrl, {}); // when sending, add to body what you want
    }
  }
