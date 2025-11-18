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
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../shared/confirmation-dialog.component';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';

const MAX_SIZE = 2 * 1024 * 1024; // 2 MB

interface ImagePreview {
  url: string;
  isNew: boolean; // To distinguish between existing (id) and new (file)
  identifier: string | File; // imageId or File object
}

@Component({
  selector: 'app-manage-products',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, ImageUrlPipe],
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
    rejectedFiles: string[] = [];
    imagePreviews: ImagePreview[] = [];
    deletedImageIds: string[] = []; // track images user removed during edit


    constructor(
      private route: ActivatedRoute,
      private productService: ProductService,
      private router: Router,
      private userService: UserService,
      public dialog: MatDialog
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
                // ensure images array exists
                if (!this.product.images) this.product.images = [];
                this.resetStagedChanges();
                this.updateImagePreviews();
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
        if (!input.files || input.files.length === 0) return;

        // Reset per-selection state
        this.error = null;
        this.rejectedFiles = [];

        const totalCurrentImageCount = this.imagePreviews.length;
        const allowedNewFiles = 5 - totalCurrentImageCount;

        if (allowedNewFiles <= 0) {
          this.error = 'You have already reached the maximum of 5 images.';
          return;
        }

        // Enforce count first
        const candidateFiles = Array.from(input.files).slice(0, allowedNewFiles);
        let countError: string | null = null;
        if (input.files.length > allowedNewFiles) {
          countError = `You can only add ${allowedNewFiles} more image(s). Extra files were ignored.`;
        }

        // Size validation
        const validFiles: File[] = [];
        candidateFiles.forEach(file => {
          if (file.size > MAX_SIZE) {
            this.rejectedFiles.push(
              `${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)`
            );
          } else {
            validFiles.push(file);
          }
        });

        let sizeError: string | null = null;
        if (this.rejectedFiles.length) {
          sizeError = `Files can only be up to 2 MB, you have: ${this.rejectedFiles.join(', ')}`;
        }

        // Combine errors if both occurred
        if (countError || sizeError) {
          this.error = [countError, sizeError].filter(Boolean).join(' | ');
        }

        // Accept valid files
        validFiles.forEach(f => this.selectedFiles.push(f));

        // Refresh previews
        this.updateImagePreviews();

        // Clear input so same files can be re-selected if needed
        input.value = '';
      }

      loadMyProducts() {
          this.loading = true;
          this.error = null;
          this.userService.getMe().subscribe({
            next: (user: User) => {
              this.sellerProducts = (user.products || []).reverse();
              console.log('getMyProducts: success', this.sellerProducts);
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
              formData.append('price', (this.product.price ?? 0).toString());
              formData.append('quantity', (this.product.quantity ?? 0).toString());

              if (this.mode === 'create') {
                  // create endpoint expects key 'imagesList'
                  this.selectedFiles.forEach(file => {
                     formData.append('imagesList', file);
                  });

                  this.productService.createProduct(formData).subscribe({
                    next: () => this.onSuccess(),
                    error: (err) => console.error('Create failed', err)
                  });
              } else {
                  this.deletedImageIds.forEach(id => formData.append('deletedImageIds', id));
                  this.selectedFiles.forEach(file => {
                     formData.append('images', file);
                  });

                  this.productService.updateProduct(this.productId, formData).subscribe({
                    next: () => {
                      // clear staging
                      this.deletedImageIds = [];
                      this.selectedFiles = [];
                      this.onSuccess();
                    },
                    error: (err) => console.error('Update failed', err)
                  });
              }
          }

      onDelete(): void {
        const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
          width: '350px',
          data: {
            title: 'Delete Product',
            message: 'Are you sure you want to delete this product?'
          }
        });

        dialogRef.afterClosed().subscribe(result => {
          // The user confirmed the deletion
          if (result) {
            this.productService.deleteProduct(this.productId).subscribe({
              next: () => {
                this.onSuccess();
              },
              error: (err) => {
                console.error('Delete failed', err);
                this.error = 'Failed to delete the product.';
              }
            });
          }
        });
      }

      removeImagePreview(previewToRemove: ImagePreview) {
              if (previewToRemove.isNew) {
                this.removeNewImage(previewToRemove.identifier as File);
              } else {
                this.removeExistingImage(previewToRemove.identifier as string);
              }
            }

      // remove an already uploaded image (mark for deletion and remove from view)
      private removeExistingImage(imageId: string) {
              this.product.images = (this.product.images || []).filter(i => i !== imageId);
              this.deletedImageIds.push(imageId);
              this.updateImagePreviews();
      }
      // remove a newly selected image before upload
      private removeNewImage(fileToRemove: File) {
              this.selectedFiles = this.selectedFiles.filter(f => f !== fileToRemove);
              this.updateImagePreviews();
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
              this.resetStagedChanges();
              if (this.router.url !== '/products/manage') {
                  this.router.navigate(['/products/manage']);
              }
          }
      private resetStagedChanges() {
        this.deletedImageIds = [];
        this.imagePreviews = [];
        this.selectedFiles = [];
      }

       private updateImagePreviews() {
                      const existing = (this.product.images || []).map(id => ({
                          url: id,
                          isNew: false,
                          identifier: id
                      }));

                      const newFiles = this.selectedFiles.map(file => {
                          return {
                              url: URL.createObjectURL(file),
                              isNew: true,
                              identifier: file
                          };
                      });

                      this.imagePreviews = [...existing, ...newFiles];
              }
}
