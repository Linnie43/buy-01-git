import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';
import { Router } from '@angular/router';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { ImageCarouselComponent } from '../shared/image-carousel/image-carousel.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ImageUrlPipe, ImageCarouselComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    products: Product[] = [];
    isLoggedIn = false;

    constructor(
        private productService: ProductService,
        private authService: AuthService,
        private router: Router
      ) {}

     ngOnInit() {
        this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;

        // getting the products from the backend
        this.productService.getAllProducts().subscribe({
          next: (data: Product[]) => (this.products = data.reverse()),
          error: (err: unknown) => console.error(err)
        });
      console.log(this.products);
      }

    goToProduct(productId: string) {
      this.router.navigate(['/products', productId]);
    }
}
