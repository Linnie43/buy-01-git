import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { OrderDashboardDTO, OrderStatusList } from '../../models/order.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-dashboard.component.html',
  styleUrls: ['./client-dashboard.component.css']
})
export class ClientDashboardComponent implements OnInit {
  isLoading = true;
  errorMessage = '';
  dashboardData: OrderDashboardDTO | null = null;
  filteredOrders: any[] = [];

  // Filter properties
  statusFilter = '';
  dateFilter = '';
  public orderStatuses = OrderStatusList;

  constructor(private orderService: OrderService, private router: Router) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe({
      next: (data: OrderDashboardDTO) => {
        this.dashboardData = data;
        this.applyFilters(); // Apply filters on initial load
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Failed to load dashboard data. Please try again later.';
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    if (!this.dashboardData?.orders) {
      this.filteredOrders = [];
      return;
    }

    let orders = [...this.dashboardData.orders];

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
