import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartResponseDTO, CartItemRequestDTO, CartItemUpdateDTO } from '../models/cart.model';
import { BASE_URL } from '../constants/constants';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private apiUrl = `${BASE_URL}/api/cart`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    let headers = new HttpHeaders();
    if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
    }
      return headers;
    }

//HTTP QUERIES FOR CART OPERATIONS
  addToCart(newItem: CartItemRequestDTO): Observable<CartResponseDTO> {
    return this.http.post<CartResponseDTO>(this.apiUrl, newItem, { headers: this.getAuthHeaders() });
  }

  getCart(): Observable<CartResponseDTO> {
      return this.http.get<CartResponseDTO>(this.apiUrl, { headers: this.getAuthHeaders() });
    }

  updateCartItem(productId: string, update: CartItemUpdateDTO): Observable<CartResponseDTO> {
    return this.http.put<CartResponseDTO>(`${this.apiUrl}/${productId}`, update, { headers: this.getAuthHeaders() });
  }

  deleteItem(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}`, { headers: this.getAuthHeaders() });
  }

  clearCart(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/all`, { headers: this.getAuthHeaders() });
  }
}
