import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { CartResponseDTO, ItemDTO } from '../../models/cart.model';
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit, OnDestroy {
  cart: CartResponseDTO | null = null;

  private timerId: number | null = null;
  timeLeftMs = 0;

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.loadCart();
    }
  }

  ngOnDestroy(): void {
    this.stopTimer();
  }

  get timeLeftText(): string {
    const clamped = Math.max(0, this.timeLeftMs);
    const totalSeconds = Math.floor(clamped / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    const mm = minutes.toString().padStart(2, '0');
    const ss = seconds.toString().padStart(2, '0');
    return `${mm}:${ss}`;
  }

  get isExpiringSoon(): boolean {
    return this.timeLeftMs > 0 && this.timeLeftMs <= 60_000;
  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: (data: CartResponseDTO) => {
        this.cart = data;
        this.startOrRestartTimer();
      },
      error: (err: unknown) => {
        console.error('Error loading cart:', err);
        this.stopTimer();
      }
    });
  }

  private startOrRestartTimer(): void {
    this.stopTimer();

    const expiryIso = this.cart?.expiryTime;
    if (!expiryIso) {
      this.timeLeftMs = 0;
      return;
    }

    const expiryMs = new Date(expiryIso).getTime();
    if (Number.isNaN(expiryMs)) {
      this.timeLeftMs = 0;
      return;
    }

    const tick = () => {
      this.timeLeftMs = Math.max(0, expiryMs - Date.now());

      if (this.timeLeftMs <= 0) {
        this.stopTimer();
        this.snackBar.open('Cart hold expired. Refreshing cart\\.', 'Close', { duration: 3000 });
        this.loadCart();
      }
    };

    tick();
    this.timerId = window.setInterval(tick, 1000);
  }

  private stopTimer(): void {
    if (this.timerId !== null) {
      window.clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  updateQuantity(item: ItemDTO, newQty: number): void {
    if (newQty <= 0 || item.updating) {
      return;
    }

    item.updating = true;

    this.cartService.updateCartItem(item.productId, { quantity: newQty }).subscribe({
      next: (res: CartResponseDTO) => {
        this.cart = res;
        this.startOrRestartTimer();

        const updatedItem = res.items.find(i => i.productId === item.productId);
        if (updatedItem) {
          item.quantity = updatedItem.quantity;
        }

        item.updating = false;
      },
      error: () => {
        item.updating = false;

        this.snackBar.open(
          'Cannot add more than available quantity\\!',
          'Close',
          { duration: 3000, panelClass: ['snack-bar-error'] }
        );
      }
    });
  }

  removeItem(item: ItemDTO): void {
    this.cartService.deleteItemById(item.productId).subscribe({
      next: () => this.loadCart(),
      error: (err: unknown) => console.error('Error removing item from cart:', err)
    });
  }

  proceedToCheckout(): void {
    this.router.navigate(['/checkout']);
  }
}
