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

  ngOnInit(): void {
    this.cartService.updateCartStatus({ cartStatus: CartStatus.CHECKOUT }).subscribe({
      next: () => console.log('Cart status updated to CHECKOUT'),
      error: (err: any) => console.error('Failed to update cart status', err)
    });
  }

  // Triggers when user navigates away (Back button, Home link, etc.)
  ngOnDestroy(): void {
    // Only call the backend to revert to ACTIVE if an order wasn't placed
    if (!this.createdOrder) {
      this.cartService.updateCartStatus({ cartStatus: CartStatus.ACTIVE }).subscribe({
        next: () => console.log('Cart status reverted to ACTIVE (User left checkout)'),
        error: (err) => console.error('Failed to revert cart status', err)
      });
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
