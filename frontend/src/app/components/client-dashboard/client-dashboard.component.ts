import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../services/order.service';
import { OrderDashboardDTO, ItemDTO, OrderResponseDTO } from '../../models/order.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './client-dashboard.component.html',
  styleUrls: ['./client-dashboard.component.css']
})
export class ClientDashboardComponent implements OnInit {
  isLoading = true;
  errorMessage: string | null = null;
  totalSpent = 0;
  mostBoughtItems: ItemDTO[] = [];
  orders: OrderResponseDTO[] = [];

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.orderService.getOrders().subscribe({
      next: (dashboardData: OrderDashboardDTO) => {
        this.totalSpent = dashboardData.total;
        this.mostBoughtItems = dashboardData.topItems;
        this.orders = dashboardData.orders;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load dashboard data. Please try again later.';
        this.isLoading = false;
        console.error(err);
      }
    });
  }
}
