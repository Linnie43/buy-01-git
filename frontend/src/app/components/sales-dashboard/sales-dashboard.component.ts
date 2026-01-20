import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { ItemDTO, OrderResponseDTO, OrderStatusList } from '../../models/order.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sales-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './sales-dashboard.component.html',
  styleUrl: './sales-dashboard.component.css'
})
export class SalesDashboardComponent implements OnInit {
  orders: OrderResponseDTO[] = [];
  filteredOrders: OrderResponseDTO[] = [];
  topSellingItems: ItemDTO[] = [];
  totalSales = 0;
  totalOrders = 0;
  totalUnitsSold = 0;
  isLoading = true;
  errorMessage: string | null = null;

  // Filter properties
  statusFilter = '';
  dateFilter = '';
  public orderStatuses = OrderStatusList;

  constructor(private readonly orderService: OrderService, private readonly router: Router) {}

  ngOnInit(): void {
    this.loadSalesData();
  }

  loadSalesData(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.orderService.getOrders().subscribe({
      next: (dashboardData) => {
        this.orders = dashboardData.orders;
        this.totalSales = dashboardData.total;
        this.totalOrders = dashboardData.orders.length;
        this.topSellingItems = dashboardData.topItems;

        this.totalUnitsSold = dashboardData.orders
          .filter(order => order.status !== 'CANCELLED')
          .flatMap(order => order.items)
          .reduce((sum, item) => sum + item.quantity, 0);

        this.applyFilters(); // Apply filters on initial load
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load sales data', err);
        this.errorMessage = 'Failed to load sales data. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    let orders = [...this.orders];

    // Filter by status
    if (this.statusFilter) {
      orders = orders.filter(order => order.status === this.statusFilter);
    }

    // Filter by date
    if (this.dateFilter) {
      orders = orders.filter(order => {
        const orderDate = new Date(order.createdAt).toISOString().split('T')[0];
        return orderDate === this.dateFilter;
      });
    }

    this.filteredOrders = orders;
  }

  clearFilters(): void {
    this.statusFilter = '';
    this.dateFilter = '';
    this.applyFilters();
  }

  goToProduct(productId: string): void {
    this.router.navigate(['/products', productId]);
  }

  viewOrder(orderId: string): void {
    this.router.navigate(['/order', orderId]);
  }
}
