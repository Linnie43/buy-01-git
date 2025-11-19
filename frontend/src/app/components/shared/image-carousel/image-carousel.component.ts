import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImageUrlPipe } from '../../../pipes/image-url.pipe';

@Component({
  selector: 'app-image-carousel',
  standalone: true,
  imports: [CommonModule, ImageUrlPipe],
  templateUrl: './image-carousel.component.html',
  styleUrls: ['./image-carousel.component.css']
})
export class ImageCarouselComponent {
  @Input() images: string[] = [];
  @Input() placeholder: string = 'assets/product_image_placeholder.png';
  @Input() objectFit: 'cover' | 'contain' = 'cover';

  currentImageIndex = 0;

  get currentImageSrc(): string {
    if (!this.images || this.images.length === 0) {
      return this.placeholder;
    }
    return this.images[this.currentImageIndex];
  }

  nextImage(event: Event): void {
    event.stopPropagation();
    if (this.images.length > 1) {
      this.currentImageIndex = (this.currentImageIndex + 1) % this.images.length;
    }
  }

  previousImage(event: Event): void {
    event.stopPropagation();
    if (this.images.length > 1) {
      this.currentImageIndex = (this.currentImageIndex - 1 + this.images.length) % this.images.length;
    }
  }
}
