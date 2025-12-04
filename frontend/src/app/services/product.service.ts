import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, map } from 'rxjs';
import { Product } from '../models/product.model';
import { BASE_URL } from '../constants/constants';
import { tap } from 'rxjs/operators';
import { MEDIA_BASE_URL } from '../constants/constants';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private apiUrl = `${BASE_URL}/api/products`;  // endpoint

  constructor(private http: HttpClient) {}

  private mapProduct(item: any): Product {
      const imagePaths = (item?.images || []).map((imgId: string) => {
        if (imgId && !imgId.startsWith('http') && !imgId.startsWith('/')) {
          return `${imgId}`;
        }
        return imgId;
      }).filter(Boolean);

      return {
        productId: item.productId,
        name: item.name,
        description: item.description,
        price: item.price,
        quantity: item.quantity,
        ownerId: item.ownerId,
        images: imagePaths.length > 0 ? imagePaths : ['assets/product_image_placeholder.png'],
        isProductOwner: item.isProductOwner
      };
    }

    // getting all products with the http call
    getAllProducts(): Observable<Product[]> {
      return this.http.get<any[]>(this.apiUrl).pipe(
        map(products => products.map(p => this.mapProduct(p)))
      );
    }


    // getting the product by id with the http call
   getProductById(productId: string): Observable<Product> {
      return this.http.get<any>(`${this.apiUrl}/${productId}`).pipe(
        map(product => this.mapProduct(product))
      );
    }

    // updating the product with the http call
   updateProduct(productId: string, formData: FormData): Observable<Product> {
      return this.http.put<any>(`${this.apiUrl}/${productId}`, formData).pipe(
        map(response => this.mapProduct(response))
      );
    }

   createProduct(formData: FormData): Observable<Product> {
      return this.http.post<any>(`${this.apiUrl}`, formData).pipe(
        map(response => this.mapProduct(response))
      );
    }

   deleteProduct(id: string): Observable<void> {
       return this.http.delete<void>(`${this.apiUrl}/${id}`);
     }
  }



