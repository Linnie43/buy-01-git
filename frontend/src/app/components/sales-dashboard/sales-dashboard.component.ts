import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../services/order.service';
import { ItemDTO, OrderResponseDTO } from '../../models/order.model'; // Import ItemDTO

@Component({
  selector: 'app-sales-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sales-dashboard.component.html',
  styleUrl: './sales-dashboard.component.css'
})
export class SalesDashboardComponent implements OnInit {
  orders: OrderResponseDTO[] = [];
  topSellingItems: ItemDTO[] = []; // To store top selling items
  totalSales = 0;
  totalOrders = 0;
  totalUnitsSold = 0; // To store total units sold
  isLoading = true;
  errorMessage: string | null = null;

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
        this.topSellingItems = dashboardData.topItems;

        // Calculate total units sold by summing quantities from all items in all orders
        this.totalUnitsSold = dashboardData.orders.reduce((total, order) =>
          total + order.items.reduce((subTotal, item) => subTotal + item.quantity, 0), 0);

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
