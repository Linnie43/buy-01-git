import { Pipe, PipeTransform } from '@angular/core';
import { MEDIA_BASE_URL } from '../constants/constants';

@Pipe({
  name: 'imageUrl',
  standalone: true,
})
// Pipe to transform image IDs into full URLs or return a placeholder
export class ImageUrlPipe implements PipeTransform {
  transform(imageId: string | undefined | null, placeholder: string = 'assets/product_image_placeholder.png'): string {
      // If imageId is falsy (null, undefined, empty string), return the placeholder.
     if (!imageId) {
          return placeholder;
        }

     // If it's already a local asset path, return it directly.
     if (imageId.startsWith('assets/')) {
          return imageId;
        }

      console.log('Transforming imageId:', imageId);
      return `${MEDIA_BASE_URL}/${imageId}`;

  }
}
