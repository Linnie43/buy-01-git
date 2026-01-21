import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { Category, Product } from '../../models/product.model';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmationDialogComponent } from '../shared/confirmation-dialog.component';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { ImageCarouselComponent } from '../shared/image-carousel/image-carousel.component';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

const MAX_SIZE = 2 * 1024 * 1024; // 2 MB

interface ImagePreview {
  url: string;
  isNew: boolean; // To distinguish between existing (id) and new (file)
  identifier: string | File; // imageId or File object
}

@Component({
  selector: 'app-manage-products',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, ImageUrlPipe, ImageCarouselComponent, MatSnackBarModule],
  templateUrl: './manage-products.component.html',
  styleUrls: ['./manage-products.component.css']
})
export class ManageProductsComponent implements OnInit {
    mode: 'create' | 'update' = 'create'
    productForm: FormGroup;
    productId: string = '';
    sellerProducts: Product[] = [];
    loading = false;
    error: string | null = null;
    formErrors: { [key: string]: string } = {};
    selectedFiles: File[] = [];
    rejectedFiles: string[] = [];
    imagePreviews: ImagePreview[] = [];
    deletedImageIds: string[] = [];
    categories = Object.values(Category);

    constructor(
      private readonly fb: FormBuilder,
      private readonly route: ActivatedRoute,
      private readonly productService: ProductService,
      private readonly router: Router,
      private readonly userService: UserService,
      public dialog: MatDialog,
      private readonly snackBar: MatSnackBar
    ) {
      this.productForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(255)]],
            description: ['', [Validators.required, Validators.maxLength(2000)]],
            price: [null, [Validators.required, Validators.min(0.01), Validators.max(100000)]],
            quantity: [0, [Validators.required, Validators.min(0), Validators.max(1000), Validators.pattern("^[0-9]*$")]],
            category: [null, [Validators.required]]
          });
      }

  ngOnInit() {
        // Subscribe to route changes to handle navigation between update and create
        this.route.paramMap.subscribe(params => {
          this.productId = params.get('id') || '';
          if (this.productId) {
            this.mode = 'update';
            this.productService.getProductById(this.productId).subscribe({
              next: (data: Product) => {
                console.log('Product loaded:', data);
                this.productForm.patchValue(data);
                const images = data.images || [];
                this.resetStagedChanges(images);
                },
              error: (err: any) => {
                console.error('Failed to load product for editing', err);
                this.router.navigate(['/products/manage']);
              }
            });
          } else {
            this.switchToCreateMode();
          }
        });
        this.loadMyProducts();
      }

      // Handle file selection
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

        // Get names of already selected files for uniqueness check
        const existingFileNames = new Set(this.selectedFiles.map(f => f.name));
        const duplicateFiles: string[] = [];

        // Size and uniqueness validation
        const validFiles: File[] = [];
        candidateFiles.forEach(file => {
          if (existingFileNames.has(file.name)) {
            duplicateFiles.push(file.name);
          } else if (file.size > MAX_SIZE) {
            this.rejectedFiles.push(
              `${file.name} (${(file.size / 1024 / 1024).toFixed(2)} MB)`
            );
          } else {
            validFiles.push(file);
            existingFileNames.add(file.name); // Add to set to check for duplicates within the same selection
          }
        });

        let sizeError: string | null = null;
        if (this.rejectedFiles.length) {
          sizeError = `Files can only be up to 2 MB, you have: ${this.rejectedFiles.join(', ')}`;
        }

       let duplicateError: string | null = null;
       if (duplicateFiles.length > 0) {
         duplicateError = `Duplicate file names are not allowed: ${duplicateFiles.join(', ')}`;
       }

        // Combine errors if both occurred
        this.error = [countError, sizeError, duplicateError].filter(Boolean).join(' | ');

        // Accept valid files
        validFiles.forEach(f => this.selectedFiles.push(f));

        // Refresh previews
        const existingImageIds = this.imagePreviews.filter(p => !p.isNew).map(p => p.identifier as string);
        this.updateImagePreviews(existingImageIds);

        // Clear input so same files can be re-selected if needed
        input.value = '';
      }

      // Load products of the logged-in seller
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
              this.error = 'Failed to load your products.';
              this.loading = false;
            }
          });
        }

      // Handle form submission for create/update
      submit() {
              this.error = null;
              this.formErrors = {};

               // Trim string values before validation and submission
               const nameControl = this.productForm.get('name');
               if (nameControl && typeof nameControl.value === 'string') {
                 nameControl.setValue(nameControl.value.trim());
               }
               const descriptionControl = this.productForm.get('description');
                 if (descriptionControl && typeof descriptionControl.value === 'string') {
                   // Replace multiple newlines with a single one, then trim.
                   const cleanedDescription = descriptionControl.value.replace(/(\r\n|\r|\n){3,}/g, '\n\n').trim();
                   descriptionControl.setValue(cleanedDescription);
                  }

              this.productForm.markAllAsTouched();

              if (this.productForm.invalid) {
                return;
              }

              const formData = new FormData();
              Object.keys(this.productForm.controls).forEach(key => {
                    formData.append(key, this.productForm.get(key)?.value);
                  });

              if (this.mode === 'create') {
                    this.selectedFiles.forEach(file => formData.append('imagesList', file));
                    this.productService.createProduct(formData).subscribe({
                      next: () => {
                        this.snackBar.open('Product created successfully!', 'Close', { duration: 3000, panelClass: ['snack-bar-success'] });
                        this.onSuccess();
                      },
                      error: (err) => this.handleError(err, 'Create')
                    });

                   } else {
                        this.deletedImageIds.forEach(id => formData.append('deletedImageIds', id));
                        this.selectedFiles.forEach(file => formData.append('images', file));
                        this.productService.updateProduct(this.productId, formData).subscribe({
                          next: () => {
                            this.snackBar.open('Product updated successfully!', 'Close', { duration: 3000, panelClass: ['snack-bar-success'] });
                            // Refresh product list
                            this.loadMyProducts();
                          },
                          error: (err) => this.handleError(err, 'Update')
                        });
                      }
                    }

      private handleError(err: HttpErrorResponse, action: 'Create' | 'Update') {
          console.error(`${action} failed`, err);
          if (err.status === 400 && err.error && typeof err.error === 'object') {
             if (err.error.error && typeof err.error.error === 'string' && err.error.error.includes('Product name must be between')) {
              this.formErrors['name'] = err.error.error;
            } else if (err.error.error === 'Invalid file type') {
              this.error = 'One or more images have an unsupported file type. Please use JPG, PNG, or GIF.';
            } else {
              this.formErrors = err.error;
            }
          } else if (err.status !== 401 && err.status !== 403) {
            this.error = `Failed to ${action.toLowerCase()} the product. Please try again.`;
          }
        }

      // Product deletion and confirmation
      onDelete(): void {
        const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
          width: '350px',
          data: {
            title: 'Delete Product',
            message: 'Are you sure you want to delete this product?'
          },
        });

        dialogRef.afterClosed().subscribe(result => {
              if (result) {
                this.productService.deleteProduct(this.productId).subscribe({
                  next: () => {
                    this.snackBar.open('Product deleted', 'Close', { duration: 3000, panelClass: ['snack-bar-success'] });
                    this.onSuccess();
                  },
                  error: (err) => { this.error = 'Failed to delete the product.'; }
                });
              }
            });
          }

      // Remove an image preview and update staged changes
      removeImagePreview(previewToRemove: ImagePreview) {
          const currentImages = this.imagePreviews.filter(p => !p.isNew).map(p => p.identifier as string);
          if (previewToRemove.isNew) {
            this.selectedFiles = this.selectedFiles.filter(f => f !== previewToRemove.identifier);
          } else {
            this.deletedImageIds.push(previewToRemove.identifier as string);
          }
          this.updateImagePreviews(currentImages.filter(id => id !== previewToRemove.identifier));
        }

      // Reset form and reload products
      private onSuccess() {
        this.loadMyProducts();
        this.switchToCreateMode();
      }

     // Navigate to edit mode for a specific product
     edit(p: Product) {
       this.router.navigate(['/products/update', p.productId]);
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }

     // Switch to create mode and reset form
     switchToCreateMode() {
         this.mode = 'create';
         this.productForm.reset({ price: 0, quantity: 1 });
         this.resetStagedChanges();
         if (this.router.url !== '/products/manage') {
           this.router.navigate(['/products/manage']);
         }
       }

     // Reset staged changes: selected files, deleted images, previews, errors
     private resetStagedChanges(images: string[] = []) {
          this.deletedImageIds = [];
          this.selectedFiles = [];
          this.updateImagePreviews(images);
          this.error = null;
          this.formErrors = {};
        }

      // Update image previews based on existing image IDs and newly selected files
      private updateImagePreviews(existingImageIds: string[]) {
         const filteredIds = existingImageIds.filter(id =>
             id && id !== 'default_product.png' && id !== 'assets/product_image_placeholder.png'
           );

           const existing = filteredIds.map(id => ({
             url: id,
             isNew: false,
             identifier: id
           }));

           const newFiles = this.selectedFiles.map(file => ({
             url: URL.createObjectURL(file),
             isNew: true,
             identifier: file
           }));

           this.imagePreviews = [...existing, ...newFiles];
        }
      }
