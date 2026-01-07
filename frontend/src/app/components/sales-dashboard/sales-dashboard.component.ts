import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service'; // Import the service
import { OrderResponseDTO } from '../../models/order.model'; // Import the model

@Component({
  selector: 'app-sales-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sales-dashboard.component.html',
  styleUrl: './sales-dashboard.component.css'
})
export class SalesDashboardComponent implements OnInit {
  orders: OrderResponseDTO[] = [];
  totalSales = 0;
  totalOrders = 0;
  isLoading = true;
  errorMessage: string | null = null;

  // Inject the OrderService
  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadSalesData();
  }

  loadSalesData(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.orderService.getSalesDashboard().subscribe({
      next: (dashboardData) => {
        this.orders = dashboardData.orders;
        this.totalSales = dashboardData.total;
        this.totalOrders = dashboardData.orders.length;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load sales data', err);
        this.errorMessage = 'Failed to load sales data. Please try again later.';
        this.isLoading = false;
      }
    });
  }
}
