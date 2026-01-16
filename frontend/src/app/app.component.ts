import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { AVATAR_BASE_URL } from './constants/constants';
import { CartService } from './services/cart.service';
import { AuthService } from './services/auth.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterLink, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  profileRoute = '/auth';
  profileImageUrl: string | null = null;
  isLoggedIn = false;
  showProfile = false;
  isSeller = false;
  isOnAuthPage = false;
  cartItemCount = 0;


  constructor(private auth: AuthService, private router: Router, private cartService: CartService) {
    this.updateProfileState();
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.updateProfileState();
      }
    });
  }

  ngOnInit(): void {
    this.cartService.cart$.subscribe(cart => {
      this.cartItemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;
    });

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      const isLoggedIn = this.auth.isLoggedIn();
      if (isLoggedIn && this.auth.getUserRole() === 'CLIENT') {
        this.cartService.loadCart();
      } else {
        this.cartService.clearCart();
      }
    });
  }


  private updateProfileState() {
    this.isLoggedIn = this.auth.isLoggedIn();
    const role = this.auth.getUserRole();
    this.isSeller = role === 'SELLER';
    this.profileRoute =
      role === 'CLIENT' ? '/client-profile' :
      role === 'SELLER' ? '/seller-profile' :
      '/auth';

    const url = this.router.url ?? '';
    this.showProfile = this.isLoggedIn && !url.includes('auth');
    this.isOnAuthPage = url.includes('auth');

    if (this.isLoggedIn) {
        this.auth.getCurrentUser().subscribe(user => {
          this.profileImageUrl = user.avatar
            ? `${AVATAR_BASE_URL}/${user.avatar}`
            : 'assets/default.jpg';
        });
      } else {
        this.profileImageUrl = 'assets/default.jpg';
      }
  }

  logout() {
    this.auth.logout();
    this.isSeller = false;
    this.showProfile = false;
    this.cartService.clearCart();
  }
}
