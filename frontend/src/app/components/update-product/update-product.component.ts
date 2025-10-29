import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-update-product',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './update-product.component.html',
  styleUrl: './update-product.component.css'
})
export class UpdateProductComponent implements OnInit {
  product: Product = {
      productId: '',
        name: '',
        description: '',
        price: 0,
        quantity: 0,
        sellerId: ''
    };

  productId: string = '';

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private router: Router
  ) {}

   ngOnInit() {
      this.productId = this.route.snapshot.paramMap.get('id') || '';
      if (this.productId) {
        this.productService.getProductById(this.productId).subscribe({
          next: (data: Product) => (this.product = data),
          error: (err: any) => console.error(err)
        });
      }
    }

  updateProduct() {
      if (!this.product) return;
      this.productService.updateProduct(this.productId, this.product).subscribe({
        next: (data: Product) => {
          console.log('Product updated:', data);
          this.router.navigate(['/dashboard']);
        },
        error: (err: any) => console.error(err)
      });
    }

}
