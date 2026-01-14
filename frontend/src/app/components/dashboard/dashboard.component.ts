import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Import FormsModule
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';
import { Router } from '@angular/router';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { ImageCarouselComponent } from '../shared/image-carousel/image-carousel.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ImageUrlPipe, ImageCarouselComponent, FormsModule], // Add FormsModule
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
    products: Product[] = [];
    isLoggedIn = false;

    searchTerm: string = '';
    sortBy: string = 'latest';

    constructor(
        private productService: ProductService,
        private authService: AuthService,
        private router: Router
      ) {}

     ngOnInit() {
        this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;

        // getting the products from the backend
        this.productService.getAllProducts(
          undefined, undefined, undefined, undefined, 0, 10
        ).subscribe({
          next: (data: { products: Product[]; total: number }) => {
            this.products = data.products.reverse();
            console.log(this.products);
          },
          error: (err: unknown) => console.error(err)
        });
      }

    goToProduct(productId: string) {
      this.router.navigate(['/products', productId]);
    }

    // Placeholder methods, change for backend!
    onSearch() {
      console.log('Searching for:', this.searchTerm);
    }

    onSortChange() {
      console.log('Sorting by:', this.sortBy);
    }
}
