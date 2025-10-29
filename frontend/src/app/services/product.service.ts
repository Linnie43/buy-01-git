import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, map } from 'rxjs';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private apiUrl = 'https://localhost:8443/products'; // endpoint

  constructor(private http: HttpClient) {}

 getAllProducts(): Observable<Product[]> {
   return this.http.get<Product[]>(this.apiUrl).pipe(
     map(products =>
       products.map(p => ({
         ...p,
         image: p.image || 'assets/product_image_placeholder.png'
       }))
     )
   );
 }

  getProductById(productId: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${productId}`).pipe(
      map(product => ({
        ...product,
        image: product.image || 'assets/product_image_placeholder.png'
      }))
    );
  }

  updateProduct(productId: string, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${productId}`, product);
  }
}
