import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { Router, RouterModule } from '@angular/router';
import { ImageUrlPipe } from '../../pipes/image-url.pipe';

@Component({
  selector: 'app-sellerProfile',
  standalone: true,
  imports: [CommonModule, RouterModule, ImageUrlPipe],
  templateUrl: './sellerProfile.component.html',
  styleUrl: './sellerProfile.component.css'
})
export class SellerProfileComponent implements OnInit {
  user: User | null = null;
  isLoggedIn = false;

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;

    this.userService.getMe().subscribe({
      next: (data: User) => {
        this.user = data;
      },
      error: (err: unknown) => console.error(err),
    });
  }

  goToProduct(productId: string) {
    this.router.navigate(['/products', productId]);
  }
}
