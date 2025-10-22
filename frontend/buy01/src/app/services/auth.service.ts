import { Injectable } from '@angular/core';

export interface User {
  email: string;
  role: 'seller' | 'client';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  currentUserValue: User | null = null;

  constructor() {
    // example user for testing
    this.currentUserValue = { email: 'test@example.com', role: 'seller' };
  }
}
