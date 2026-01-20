import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { OrderService } from '../../services/order.service';
import { CartService } from '../../services/cart.service';
import { OrderResponseDTO, OrderStatusList } from '../../models/order.model';
import { Component, OnInit } from '@angular/core';


@Component({
  selector: 'app-order-view',
  standalone: true,
  imports: [CommonModule,],
  templateUrl: './order-view.component.html',
  styleUrl: './order-view.component.css'
})
export class OrderViewComponent implements OnInit {
  order: OrderResponseDTO | null = null;
  isLoggedIn = false;
  statusSteps: string[] = OrderStatusList as unknown as string[];
  showReorderModal = false;
  isSeller: boolean = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly orderService: OrderService,
    private readonly cartService: CartService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
      this.isLoggedIn = this.authService.isLoggedIn();
      this.isSeller = this.authService.getUserRole() === 'SELLER';
      const orderId = this.route.snapshot.paramMap.get('id');
        if (orderId) {
          this.orderService.getOrderById(orderId).subscribe({
            next: (data: OrderResponseDTO) => {
              this.order = data;
              console.log('Order details:', this.order);
            },
            error: (err: unknown) => {
                console.error('Error fetching order:', err);
            }
          });
        } else {
          this.router.navigate(['']);
        }
  }

  // Helper to determine if a step in the visual tracker should be green
  isStatusReached(step: string): boolean {
    if (!this.order?.status) return false;

    // We cast to 'any' here because sometimes the DTO status is an Enum
    // and the list is Strings, causing TypeScript to block the indexOf check.
    const currentIdx = this.statusSteps.indexOf(this.order.status as any);
    const stepIdx = this.statusSteps.indexOf(step);

    // Only return true if both statuses were found in the list (-1 means not found)
    return currentIdx !== -1 && currentIdx >= stepIdx;
  }

  getStatusIcon(step: string): string {
    switch(step) {
      case 'CREATED': return 'bi-cart';
      case 'CONFIRMED': return 'bi-check-lg';
      case 'SHIPPED': return 'bi-truck';
      case 'DELIVERED': return 'bi-house-door';
      default: return 'bi-circle';
    }
  }

  cancelOrder() {
    if (!this.order) return;

    if(confirm('Are you sure you want to cancel this order?')) {
      // Call service to cancel order
      this.orderService.cancelOrder(this.order.orderId).subscribe({
        next: (updatedOrder) => {
          this.order = updatedOrder;
          console.log('Order cancelled successfully.');

        },
        error: (err: unknown) => {
          console.error('Error cancelling order:', err);
        }
      });
    }
  }

    goToProduct(productId: string) {
      this.router.navigate(['/products', productId]);
    }

    openReorderModal() {
      this.showReorderModal = true;
    }

    confirmReorder() {
      if (!this.order) return;

      // Call service to reorder items
      this.cartService.reorderItems(this.order.orderId).subscribe({
        next: (CartResponseDTO) => {
          console.log('Items added to cart:', CartResponseDTO);
          this.showReorderModal = false;
          this.router.navigate(['/cart']);
        },
        error: (err: unknown) => {
          console.error('Error reordering items:', err);
        }
      });
    }
}
