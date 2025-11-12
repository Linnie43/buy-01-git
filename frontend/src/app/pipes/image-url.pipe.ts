import { Pipe, PipeTransform } from '@angular/core';
import { MEDIA_BASE_URL } from '../constants/constants';

@Pipe({
  name: 'imageUrl',
  standalone: true,
})
// Pipe to transform image IDs into full URLs or return a placeholder
export class ImageUrlPipe implements PipeTransform {
  transform(imageId: string | undefined | null, placeholder: string = 'assets/product_image_placeholder.png'): string {
    if (imageId) {
      return `${MEDIA_BASE_URL}/${imageId}`;
    }
    return placeholder;
  }
}
