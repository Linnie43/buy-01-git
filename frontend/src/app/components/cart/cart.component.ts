import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
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
export class CartComponent implements OnInit {
  cart: CartResponseDTO | null = null;

  constructor(private cartService: CartService, private authService: AuthService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.loadCart();
      }
  }

  loadCart(): void {
    this.cartService.getCart().subscribe({
      next: (data: CartResponseDTO) => {
        this.cart = data;
      },
      error: (err: unknown) => console.error('Error loading cart:', err)
    });
  }

  updateQuantity(item: ItemDTO, delta: number): void {
    const originalQty = item.quantity;
    const newQty = originalQty + delta;

    if (newQty <= 0 || newQty === originalQty) {
      return;
    }

    item.updating = true;

    this.cartService.updateCartItem(item.productId, { quantity: newQty }).subscribe({
      next: (res: CartResponseDTO) => {
        this.cart = res;

        const updatedItem = res.items.find(i => i.productId === item.productId);
        if (updatedItem) {
          item.quantity = updatedItem.quantity;
        } else {
          item.quantity = originalQty;
        }

        item.updating = false;
      },
      error: () => {
        item.quantity = originalQty;
        item.updating = false;

        this.snackBar.open(
                'Cannot add more than available quantity!',
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

}
