import { Injectable } from '@angular/core';
//import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

export interface Product {
  name: string;
  price: number;
  image: string;
  sellerId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  //temp mock data
    private mockProducts: Product[] = [
      { name: 'Laptop', price: 999, image: 'https://via.placeholder.com/150' },
      { name: 'Phone', price: 699, image: 'https://via.placeholder.com/150' },
      { name: 'Headphones', price: 199, image: 'https://via.placeholder.com/150' }
    ];

  //private apiUrl = 'http://localhost:8080/api/products'; // endpoint

  //constructor(private http: HttpClient) {}
  constructor() {}

  getAllProducts(): Observable<Product[]> {
    //return this.http.get<Product[]>(this.apiUrl);
    return of(this.mockProducts);
  }
}
