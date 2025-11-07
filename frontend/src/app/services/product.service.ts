import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, map } from 'rxjs';
import { Product } from '../models/product.model';
import { BASE_URL } from '../constants/constants';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private apiUrl = `${BASE_URL}/product-service/api/products`;  // endpoint

  constructor(private http: HttpClient) {}

  // getting all products with the http call
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

 // getting the product by id with the http call
  getProductById(productId: string): Observable<Product> {
    return this.http
    .get<Product>(`${this.apiUrl}/${productId}`)
    .pipe(
      tap(res => console.log('[ProductService] getProductById response:', res)),
      map(product => ({
        ...product,
        image: product.image || 'assets/product_image_placeholder.png'
      }))
    );
  }

 // updating the product with the http call
  updateProduct(productId: string, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${productId}`, product);
  }

  createProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}`, product);
  }


}
