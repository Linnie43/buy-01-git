import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-product-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-view.component.html',
  styleUrl: './product-view.component.css'
})
export class ProductViewComponent implements OnInit {
  product: Product | null = null;
  isLoggedIn = false;
  currentUserId: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private authService: AuthService,
    private router: Router
  ) {}

ngOnInit(): void {
  this.isLoggedIn = this.authService.isLoggedIn();
  this.currentUserId = this.authService.getCurrentUserId();

  const productId = this.route.snapshot.paramMap.get('id');
  if (productId) {
    this.productService.getProductById(productId).subscribe({
      next: (data: Product) => (this.product = data),
      error: (err: unknown) => console.error('Error fetching product:', err)
    });
  }
}
 isOwner(product: Product): boolean {
   return this.currentUserId === product.sellerId;
 }


  goToUpdateProduct(productId: string) {
    this.router.navigate(['/products/update', productId]);
  }
}
