import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { ImageCarouselComponent } from '../shared/image-carousel/image-carousel.component';

@Component({
  selector: 'app-product-view',
  standalone: true,
  imports: [CommonModule, ImageUrlPipe, ImageCarouselComponent],
  templateUrl: './product-view.component.html',
  styleUrl: './product-view.component.css'
})
export class ProductViewComponent implements OnInit {
  product: Product | null = null;
  isLoggedIn = false;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
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
          error: (err: unknown) => console.error('Error fetching product:', err)
        });
      } else {
        this.router.navigate(['']);
      }
    }

  goToUpdateProduct(productId: string) {
    this.router.navigate(['/products/update', productId]);
  }
}
