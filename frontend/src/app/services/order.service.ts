import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { OrderResponseDTO, Status , OrderDashboardDTO } from '../models/order.model';
import { ORDER_BASE_URL } from '../constants/constants';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private readonly apiUrl = `${ORDER_BASE_URL}`;

  constructor(private readonly http: HttpClient) {}

  private mapOrder(item: any): OrderResponseDTO {
    return {
      orderId: item.orderId,
      items: item.items.map((item: any) => ({
        productId: item.productId,
        productName: item.productName,
        quantity: item.quantity,
        price: item.price,
        subtotal: item.subtotal,
        sellerId: item.sellerId
      })),
      totalPrice: item.totalPrice,
      status: item.status as Status,
      shippingAddress: {
        fullName: item.shippingAddress.fullName,
        street: item.shippingAddress.street,
        city: item.shippingAddress.city,
        postalCode: item.shippingAddress.postalCode,
        country: item.shippingAddress.country
      },
      paid: item.paid,
      deliveryDate: item.deliveryDate ? new Date(item.deliveryDate) : null,
      trackingNumber: item.trackingNumber || null,
      createdAt: new Date(item.createdAt),
      updatedAt: item.updatedAt ? new Date(item.updatedAt) : null
    };
  }

  createOrder(orderRequest: any): Observable<OrderResponseDTO> {
    return this.http.post<any>(this.apiUrl, orderRequest).pipe(
      map(order => this.mapOrder(order))
    );
  }

  getSalesDashboard(): Observable<OrderDashboardDTO> {
    return this.http.get<OrderDashboardDTO>(this.apiUrl);
  }

  getOrders(): Observable<OrderDashboardDTO> {
    return this.http.get<OrderDashboardDTO>(this.apiUrl);
  }

  getOrderById(orderId: string): Observable<OrderResponseDTO> {
    return this.http.get<any>(`${this.apiUrl}/${orderId}`).pipe(
      map(order => this.mapOrder(order))
    );
  }

  cancelOrder(orderId: string): Observable<OrderResponseDTO> {
    return this.http
      .put<any>(`${this.apiUrl}/${orderId}`, { status: Status.CANCELLED })
      .pipe(map(order => this.mapOrder(order)));
  }
}
