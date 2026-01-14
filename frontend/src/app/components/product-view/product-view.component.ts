import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { ImageCarouselComponent } from '../shared/image-carousel/image-carousel.component';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpClientModule } from '@angular/common/http';
import { CartService } from '../../services/cart.service';
import { FormsModule } from '@angular/forms';
import { CartResponseDTO } from '../../models/cart.model';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-product-view',
  standalone: true,
  imports: [CommonModule, FormsModule, ImageUrlPipe, ImageCarouselComponent],
  templateUrl: './product-view.component.html',
  styleUrl: './product-view.component.css'
})
export class ProductViewComponent implements OnInit {
  product: Product | null = null;
  isLoggedIn = false;
  selectedQuantity = 1;
  cart: CartResponseDTO | null = null;
  updating = false;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar

  ) {}

  ngOnInit(): void {
      this.isLoggedIn = this.authService.isLoggedIn();
      const productId = this.route.snapshot.paramMap.get('id');
        if (productId) {
          this.productService.getProductById(productId).subscribe({
            next: (data: Product) => {
              this.product = data;
            },
            error: (err: unknown) => {
              if (err instanceof HttpErrorResponse && err.status === 404) {
                this.router.navigate(['/404']);
              } else {
                console.error('Error fetching product:', err);
              }
            }
          });
        } else {
          this.router.navigate(['']);
        }
      }

  goToUpdateProduct(productId: string) {
    this.router.navigate(['/products/update', productId]);
  }

  addToCart() {
      if (!this.product) return;

      this.cartService.addToCart({
        productId: this.product.productId,
        quantity: this.selectedQuantity
      }).subscribe({
        next: (res: CartResponseDTO) => {
          console.log('Added to cart', res);
          this.cart = res;
           this.snackBar.open('Product added to cart!', 'Close', {
                  duration: 3000,
                  panelClass: ['snack-bar-success']
                });
        },
        error: (err) => {
          console.error('Cannot add to cart', err);
          this.snackBar.open('Cannot add more than available quantity!', 'Close', {
                  duration: 3000,
                  panelClass: ['snack-bar-error']
                });
        }
      });
    }

  changeQuantity(delta: number) {
    if (!this.product) return;

    const newQty = this.selectedQuantity + delta;

    if (newQty <= 0 || newQty > this.product.quantity) {
      return;
    }

    this.selectedQuantity = newQty;
  }

}
