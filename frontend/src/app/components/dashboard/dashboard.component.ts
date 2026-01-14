import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product, Category } from '../../models/product.model';
import { Router } from '@angular/router';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { ImageCarouselComponent } from '../shared/image-carousel/image-carousel.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ImageUrlPipe, ImageCarouselComponent, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  products: Product[] = [];
  isLoggedIn = false;

  // Filter Query Params
  searchTerm: string = '';
  sortBy: string = 'latest';
  selectedCategory: string = '';

  // Price State (Reverted to standard inputs)
  minPrice: number | null = null;
  maxPrice: number | null = null;

  categories: string[] = Object.values(Category);

  constructor(
    private productService: ProductService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;
    this.loadProducts();
  }

  goToProduct(productId: string) {
    this.router.navigate(['/products', productId]);
  }

  // Unified trigger for all filter changes
  onFilterChange() {
    this.loadProducts();
  }

  onSearch() {
    this.loadProducts();
  }

  onSortChange() {
    this.loadProducts();
  }

  private loadProducts() {
    let sortParam = 'createdAt,desc';

    switch (this.sortBy) {
      case 'price_asc': sortParam = 'price,asc'; break;
      case 'price_desc': sortParam = 'price,desc'; break;
      case 'alpha_asc': sortParam = 'name,asc'; break;
      case 'alpha_desc': sortParam = 'name,desc'; break;
      case 'latest': default: sortParam = 'createdAt,desc'; break;
    }

    this.productService.getAllProducts(
      this.searchTerm,
      this.minPrice ?? undefined, // Pass undefined if null
      this.maxPrice ?? undefined,
      (this.selectedCategory as Category) || undefined,
      sortParam,
      0,
      10
    ).subscribe({
      next: (data) => this.products = data.products,
      error: (err) => console.error(err)
    });
  }
}
