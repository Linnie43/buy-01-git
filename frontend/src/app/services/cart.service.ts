import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { CartResponseDTO, CartItemRequestDTO, CartItemUpdateDTO, CartUpdateRequest } from '../models/cart.model';
import { BASE_URL } from '../constants/constants';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private apiUrl = `${BASE_URL}/api/cart`;
  private cartSubject = new BehaviorSubject<CartResponseDTO | null>(null);
  cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient) {}

  loadCart(): void {
    this.getCart().subscribe();
  }

  clearCart(): void {
    this.cartSubject.next(null);
  }

//HTTP QUERIES FOR CART OPERATIONS
  addToCart(newItem: CartItemRequestDTO): Observable<CartResponseDTO> {
    return this.http.post<CartResponseDTO>(this.apiUrl, newItem).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  getCart(): Observable<CartResponseDTO> {
      return this.http.get<CartResponseDTO>(this.apiUrl).pipe(
        tap(cart => this.cartSubject.next(cart))
      );
    }

  updateCartItem(productId: string, update: CartItemUpdateDTO): Observable<CartResponseDTO> {
      const formData = new FormData();
      formData.append('quantity', update.quantity.toString());
      return this.http.put<CartResponseDTO>(`${this.apiUrl}/${productId}`, formData).pipe(
        tap(cart => this.cartSubject.next(cart))
      );
    }

  reorderItems(orderId: string): Observable<CartResponseDTO> {
    return this.http.post<CartResponseDTO>(`${this.apiUrl}/reorder/${orderId}`, {}).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  deleteItemById(productId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}`).pipe(
      tap(() => this.loadCart())
    );
  }

  deleteCart(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/all`).pipe(
      tap(() => this.clearCart())
    );
  }

  updateCartStatus(status: CartUpdateRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/status`, status);
  }
}
