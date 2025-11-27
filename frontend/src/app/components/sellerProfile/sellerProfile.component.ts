import { Component, OnInit, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { AvatarService } from '../../services/avatar.service';
import { User } from '../../models/user.model';
import { Router, RouterModule } from '@angular/router';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';
import { ImageCarouselComponent } from '../shared/image-carousel/image-carousel.component';
import { AVATAR_BASE_URL } from '../../constants/constants';

@Component({
  selector: 'app-sellerProfile',
  standalone: true,
  imports: [CommonModule, RouterModule, ImageUrlPipe, ImageCarouselComponent],
  templateUrl: './sellerProfile.component.html',
  styleUrl: './sellerProfile.component.css'
})
export class SellerProfileComponent implements OnInit {
  user: User | null = null;
  isLoggedIn = false;
  profileImageUrl: string | null = null;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private avatarService: AvatarService,
    private router: Router,
    @Inject('WINDOW') private window: Window
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;

    this.authService.getCurrentUser().subscribe(user => {
              this.profileImageUrl = user.avatar
                ? `${AVATAR_BASE_URL}/${user.avatar}`
                : 'assets/default.jpg';
            });

    this.userService.getMe().subscribe({
      next: (data: User) => {
        if (data && data.products) {
                  data.products.reverse();
                }
        this.user = data;
      },
      error: (err: unknown) => console.error(err),
    });
  }

  onAvatarSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    // instant preview
    const preview = URL.createObjectURL(file);
    this.profileImageUrl = preview;

    this.avatarService.updateMyAvatar(file).subscribe({
      next: updated => {
        const filename = updated.avatar;
        this.profileImageUrl = this.avatarService.buildAvatarUrl(filename);
        if (this.user) this.user.avatar = filename;
        this.window.location.reload();
        console.debug('[SellerProfile] Avatar updated:', filename);
      },
      error: err => {
        console.error('[SellerProfile] Avatar update failed:', err);
        // revert
        this.profileImageUrl = this.avatarService.buildAvatarUrl(this.user?.avatar);
      }
    });
  }


  goToProduct(productId: string) {
    this.router.navigate(['/products', productId]);
  }
}
