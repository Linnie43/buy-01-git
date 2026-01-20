import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, map } from 'rxjs';
import { Product , Category } from '../models/product.model';
import { PRODUCT_BASE_URL } from '../constants/constants';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly apiUrl = `${PRODUCT_BASE_URL}`;

  constructor(private readonly http: HttpClient) {}

  private mapProduct(item: any): Product {
    const imagePaths = (item?.images || [])
      .map((imgId: string) => {
        if (imgId && !imgId.startsWith('http') && !imgId.startsWith('/')) {
          return `${imgId}`;
        }
        return imgId;
      })
      .filter(Boolean);

    return {
      productId: item.productId,
      name: item.name,
      description: item.description,
      price: item.price,
      quantity: item.quantity,
      category: item.category,
      userId: item.userId,
      images:
        imagePaths.length > 0
          ? imagePaths
          : ['assets/product_image_placeholder.png'],
      isProductOwner: item.isProductOwner,
    };
  }

  // getting all products with the http call
  getAllProducts(
    name?: string,
    min?: number,
    max?: number,
    category?: Category,
    sort: string = 'createTime,desc',
    page: number = 0,
    size: number = 10
  ): Observable<{ products: Product[]; total: number }> {
    let params: any = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

      if (name?.trim()) params = params.set('search', name);
      if (min !== undefined) params = params.set('minPrice', min.toString());
      if (max !== undefined) params = params.set('maxPrice', max.toString());
      if (category) params = params.set('category', category);

    return this.http.get<any>(this.apiUrl, { params }).pipe(
      map((res) => ({
        products: res.content.map((p: any) => this.mapProduct(p)),
        total: res.totalElements,
      }))
    );
  }

  // getting the product by id with the http call
  getProductById(productId: string): Observable<Product> {
    return this.http
      .get<any>(`${this.apiUrl}/${productId}`)
      .pipe(map((product) => this.mapProduct(product)));
  }

  // updating the product with the http call
  updateProduct(productId: string, formData: FormData): Observable<Product> {
    return this.http
      .put<any>(`${this.apiUrl}/${productId}`, formData)
      .pipe(map((response) => this.mapProduct(response)));
  }

  createProduct(formData: FormData): Observable<Product> {
    return this.http
      .post<any>(`${this.apiUrl}`, formData)
      .pipe(map((response) => this.mapProduct(response)));
  }

  deleteProduct(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
