import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { CartResponseDTO, ItemDTO } from '../../models/cart.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css']
})
export class CartComponent implements OnInit {
  cart: CartResponseDTO | null = null;

  constructor(private cartService: CartService, private authService: AuthService) {}

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
    if (newQty <= 0) return;
    this.cartService.updateCartItem(item.productId, { quantity: newQty}).subscribe({
      next: () => this.loadCart(),
      error: (err: unknown) => console.error('Error updating item quantity:', err)
    });
  }

  removeItem(item: ItemDTO): void {
    this.cartService.deleteItemById(item.productId).subscribe({
      next: () => this.loadCart(),
      error: (err: unknown) => console.error('Error removing item from cart:', err)
    });
  }

}
