import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-manage-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-products.component.html',
  styleUrls: ['./manage-products.component.css']
})
export class ManageProductsComponent implements OnInit {
    mode: 'create' | 'update' = 'create'
    product: Product = {
        productId: '',
          name: '',
          description: '',
          price: 0,
          quantity: 0,
          ownerId: ''
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
          this.mode = 'update';
          this.productService.getProductById(this.productId).subscribe({
            next: (data: Product) => (this.product = data),
            error: (err: any) => console.error(err)
          });
        }
      }

     submit() {

       if (this.mode === 'create') {
         // CREATE
         this.productService.createProduct(this.product).subscribe({
           next: () => this.router.navigate(['/dashboard']),
           error: (err) => console.error(err)
         });
       } else {
         // UPDATE
         this.productService.updateProduct(this.productId, this.product).subscribe({
           next: () => this.router.navigate(['/dashboard']),
           error: (err) => console.error(err)
         });
       }
     }
}
