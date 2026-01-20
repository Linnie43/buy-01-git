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

  // Price State
  minPrice: number | null = null;
  maxPrice: number | null = null;

  categories: string[] = Object.values(Category);

  // Pagination State
  currentPage: number = 0;
  pageSize: number = 12;
  totalElements: number = 0;
  totalPages: number = 0;

  constructor(
    private readonly productService: ProductService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;
    this.loadProducts();
  }

  goToProduct(productId: string) {
    this.router.navigate(['/products', productId]);
  }

  // Filter changes should reset to the first page
  onFilterChange() {
    this.currentPage = 0;
    this.loadProducts();
  }

  onSearch() {
    this.currentPage = 0;
    this.loadProducts();
  }

  onSortChange() {
    this.currentPage = 0;
    this.loadProducts();
  }

  onPageChange(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadProducts();
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  get pagesArray(): number[] {
    return new Array(this.totalPages).fill(0).map((x, i) => i);
  }

  private loadProducts() {
    let sortParam = 'createdAt,desc';

    switch (this.sortBy) {
      case 'price_asc': sortParam = 'price,asc'; break;
      case 'price_desc': sortParam = 'price,desc'; break;
      case 'alpha_asc': sortParam = 'name,asc'; break;
      case 'alpha_desc': sortParam = 'name,desc'; break;
      case 'latest': default: sortParam = 'createTime,desc'; break;
    }

    this.productService.getAllProducts(
      this.searchTerm,
      this.minPrice ?? undefined,
      this.maxPrice ?? undefined,
      (this.selectedCategory as Category) || undefined,
      sortParam,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (data) => {
        this.products = data.products;
        this.totalElements = data.total;
        this.totalPages = Math.ceil(this.totalElements / this.pageSize);
      },
      error: (err) => console.error(err)
    });
  }
}
