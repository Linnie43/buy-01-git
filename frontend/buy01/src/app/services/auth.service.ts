import { Injectable } from '@angular/core';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUser: { role: string } | null = null;

  constructor() {
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

   //for testing purpose only
   loginAsClient() {
       this.currentUser = { role: 'client' };
       localStorage.setItem('user', JSON.stringify(this.currentUser));
     }

    loginAsSeller() {
       this.currentUser = { role: 'seller' };
       localStorage.setItem('user', JSON.stringify(this.currentUser));
     }

   logout() {
       this.currentUser = null;
       localStorage.removeItem('user');
     }
  }

