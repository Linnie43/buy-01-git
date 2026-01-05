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

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
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
          alert('Product added to cart!');
        },
        error: (err) => {
          console.error('Cannot add to cart', err);
          alert('Cannot add more than available quantity!');
        }
      });
    }
}
