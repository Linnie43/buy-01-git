// in frontend/src/app/services/order.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderDashboardDTO } from '../models/order.model';
import { BASE_URL } from '../constants/constants';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${BASE_URL}/api/orders`;

  constructor(private http: HttpClient) { }

  getSalesDashboard(): Observable<OrderDashboardDTO> {
    // The AuthInterceptor will automatically add the Authorization header
    return this.http.get<OrderDashboardDTO>(this.apiUrl);
  }
}
