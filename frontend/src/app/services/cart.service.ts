import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartResponseDTO, CartItemRequestDTO, CartItemUpdateDTO, CartUpdateRequest } from '../models/cart.model';
import { BASE_URL } from '../constants/constants';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private apiUrl = `${BASE_URL}/api/cart`;

  constructor(private http: HttpClient) {}

//HTTP QUERIES FOR CART OPERATIONS
  addToCart(newItem: CartItemRequestDTO): Observable<CartResponseDTO> {
    return this.http.post<CartResponseDTO>(this.apiUrl, newItem);
  }

  getCart(): Observable<CartResponseDTO> {
      return this.http.get<CartResponseDTO>(this.apiUrl);
    }

  updateCartItem(productId: string, update: CartItemUpdateDTO): Observable<CartResponseDTO> {
      const formData = new FormData();
      formData.append('quantity', update.quantity.toString());
      return this.http.put<CartResponseDTO>(`${this.apiUrl}/${productId}`, formData);
    }

  reorderItems(orderId: string): Observable<CartResponseDTO> {
    return this.http.post<CartResponseDTO>(`${this.apiUrl}/reorder/${orderId}`, {});
  }

  deleteItemById(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}`);
  }

  deleteCart(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/all`);
  }

  updateCartStatus(status: CartUpdateRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/status`, status);
  }
}
