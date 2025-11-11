import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-manage-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './manage-products.component.html',
  styleUrls: ['./manage-products.component.css']
})
export class ManageProductsComponent implements OnInit {
    mode: 'create' | 'update' = 'create'
    product: Product = this.getInitialProductState();
    productId: string = '';
    sellerProducts: Product[] = [];
    loading = false;
    error: string | null = null;
    selectedFiles: File[] = [];
    imagePreviewUrl: string | null = null;


    constructor(
      private route: ActivatedRoute,
      private productService: ProductService,
      private router: Router,
      private userService: UserService
    ) {}

  ngOnInit() {
        // Subscribe to route changes to handle navigation between update and create
        this.route.paramMap.subscribe(params => {
          this.productId = params.get('id') || '';
          if (this.productId) {
            this.mode = 'update';
            this.productService.getProductById(this.productId).subscribe({
              next: (data: Product) => {
                (this.product = data);
                this.imagePreviewUrl = (data.images && data.images.length > 0) ? data.images[0] : null;
                },
              error: (err: any) => console.error('Failed to load product for editing', err)
            });
          } else {
            this.switchToCreateMode();
          }
        });
        this.loadMyProducts();
      }

    private getInitialProductState(): Product {
          return {
            productId: '',
            name: '',
            description: '',
            price: 0,
            quantity: 0,
            ownerId: '',
            images: []
          };
        }

       onFileSelected(event: Event): void {
               const input = event.target as HTMLInputElement;
               if (input.files && input.files.length > 0) {
                 this.selectedFiles = Array.from(input.files);
                 const reader = new FileReader();
                 reader.onload = () => {
                   this.imagePreviewUrl = reader.result as string;
                 };
                 reader.readAsDataURL(this.selectedFiles[0]);
               }
           }

      loadMyProducts() {
          this.loading = true;
          this.error = null;
          this.userService.getMe().subscribe({
            next: (user: User) => {
              this.sellerProducts = user.products || [];
              this.loading = false;
            },
            error: (err: HttpErrorResponse) => {
              console.error('getMyProducts: error', err);
              if (err.status === 0) {
                this.error = 'Network error — backend not reachable';
              } else if (err.status === 401 || err.status === 403) {
                this.error = 'Not authorized — token missing or expired';
              } else {
                this.error = err.error?.message || 'Could not load your listings';
              }
              this.loading = false;
            }
          });
        }

      submit() {
              const formData = new FormData();
              formData.append('name', this.product.name);
              formData.append('description', this.product.description);
              formData.append('price', this.product.price.toString());
              formData.append('quantity', this.product.quantity.toString());
              this.selectedFiles.forEach(file => {
                 formData.append('imagesList', file);
              });

              if (this.mode === 'create') {
                  this.productService.createProduct(formData).subscribe({
                    next: () => this.onSuccess(),
                    error: (err) => console.error('Create failed', err)
                  });
              } else {
                  // Note: This requires backend changes to accept multipart/form-data
                  this.productService.updateProduct(this.productId, formData).subscribe({
                    next: () => this.onSuccess(),
                    error: (err) => console.error('Update failed', err)
                  });
              }
          }

      private onSuccess() {
        this.loadMyProducts();
        this.switchToCreateMode(); // Reset form after success
      }

      edit(p: Product) {
       this.router.navigate(['/products/update', p.productId]);
      }

      switchToCreateMode() {
              this.mode = 'create';
              this.product = this.getInitialProductState();
              this.imagePreviewUrl = null;
              this.selectedFiles = [];
              if (this.router.url !== '/products/manage') {
                  this.router.navigate(['/products/manage']);
              }
          }
}
