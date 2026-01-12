import { Component } from '@angular/core';
import { Router } from '@angular/router';
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

  constructor(private cartService: CartService, private authService: AuthService, private snackBar: MatSnackBar, private router: Router) {}

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

  updateQuantity(item: ItemDTO, newQty: number): void {
    if (newQty <= 0 || item.updating) { // prevent unnecessary API calls
      return;
    }

    item.updating = true; // setting loading state to prevent double-clicks

    // sending the absolute qty we want to set for the item
    this.cartService.updateCartItem(item.productId, { quantity: newQty }).subscribe({
      next: (res: CartResponseDTO) => {
        this.cart = res; // update the whole cart with a fresh data

        const updatedItem = res.items.find(i => i.productId === item.productId);
        if (updatedItem) {
          item.quantity = updatedItem.quantity;
        }

        item.updating = false;
      },
      error: () => {
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

  proceedToCheckout() {
    this.router.navigate(['/checkout']);
  }

}
