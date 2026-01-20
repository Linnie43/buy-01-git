import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { CartStatus } from '../../models/cart.model';
import { OrderResponseDTO } from '../../models/order.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})
export class CheckoutComponent implements OnInit, OnDestroy {
  checkoutForm: FormGroup;
  isSubmitting = false;
  createdOrder: OrderResponseDTO | null = null;

  // Timer properties
  timeLeftSeconds = 300; // 5 minutes
  timerDisplay = '05:00';
  private timerInterval: any;
  private cartExpiry: Date | null = null;
  private shouldAbandon = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly cartService: CartService,
    private readonly orderService: OrderService,
    private readonly router: Router,
    private readonly snackBar: MatSnackBar
  ) {

    this.checkoutForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(25)]],
      street: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
      city: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      postalCode: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(10)]],
      country: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      payOnDelivery: [false, Validators.requiredTrue]
    });
  }

  // Helper for styling
  get isExpiringSoon(): boolean {
    return this.timeLeftSeconds > 0 && this.timeLeftSeconds <= 60;
  }

  ngOnInit(): void {
    // 1. Update status to CHECKOUT (Backend sets updateTime to NOW)
    this.cartService.updateCartStatus({ cartStatus: CartStatus.CHECKOUT }).subscribe({
      next: () => {
        console.log('Cart status updated to CHECKOUT');
        this.startTimer();
      },
      error: (err: any) => console.error('Failed to update cart status', err)
    });

    // 2. Fetch cart to store Global Expiry time
    this.cartService.getCart().subscribe({
      next: (cart) => {
        if (cart?.expiryTime) {
          this.cartExpiry = new Date(cart.expiryTime);
        }
      }
    });
  }

  ngOnDestroy(): void {

    // Only call the backend to revert status if an order wasn't placed
    if (!this.createdOrder && !this.shouldAbandon) {
      // If we flagged as abandoned (timeout + expiry), send ABANDONED. Else ACTIVE.
      const targetStatus = this.shouldAbandon ? CartStatus.ABANDONED : CartStatus.ACTIVE;

      this.cartService.updateCartStatus({ cartStatus: targetStatus }).subscribe({
        next: () => console.log(`Cart status reverted to ${targetStatus}`),
        error: (err) => console.error('Failed to revert cart status', err)
      });
    }
      this.stopTimer();
  }

  startTimer(): void {
    this.stopTimer();
    this.timerInterval = setInterval(() => {
      this.timeLeftSeconds--;
      this.updateTimerDisplay();

      if (this.timeLeftSeconds <= 0) {
        this.handleTimeout();
      }
    }, 1000);
  }

  stopTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
      this.timerInterval = null;
    }
  }

  updateTimerDisplay(): void {
    const m = Math.floor(this.timeLeftSeconds / 60);
    const s = this.timeLeftSeconds % 60;
    this.timerDisplay = `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  }

  handleTimeout(): void {
    this.stopTimer();
    const now = new Date();

    // Logic: If updateTime (5 min) passes...
    // UnLESS expiryTime has run out -> Abandon & Dashboard
    // Else -> Active & Cart
    if (this.cartExpiry && this.cartExpiry < now) {
      this.shouldAbandon = true;
      this.snackBar.open('Cart hold expired.', 'Close', { duration: 3000 });
      this.router.navigate(['']); // Dashboard/Home
    } else {
      this.shouldAbandon = false;
      this.snackBar.open('Checkout session timed out.', 'Close', { duration: 3000 });
      this.router.navigate(['/cart']);
    }
  }

  onSubmit(): void {
    if (this.checkoutForm.invalid) {
      return;
    }

    this.isSubmitting = true;

    const orderData = {
      shippingAddress: this.checkoutForm.value
    };

    this.orderService.createOrder(orderData).subscribe({
      next: (res) => {
        this.snackBar.open('Order placed successfully!', 'Close', {
          duration: 3000,
          panelClass: ['snack-bar-success']
        });

        // Store the response
        this.createdOrder = res;
        this.isSubmitting = false;
        this.stopTimer();
      },
      error: (err: any) => {
        console.error('Order creation failed', err);
        this.snackBar.open('Failed to place order. Please try again.', 'Close', {
          duration: 3000,
          panelClass: ['snack-bar-error']
        });
        this.isSubmitting = false;
      }
    });
  }

  onContinueShopping() {
    this.router.navigate(['']);
  }
}
