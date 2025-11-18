import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { AuthService } from './services/auth.service';
import { Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterLink, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  profileRoute = '/auth';
  profileImageUrl: string | null = null;
  isLoggedIn = false;
  showProfile = false;
  isSeller = false;


  constructor(private auth: AuthService, private router: Router) {
    this.updateProfileState();
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.updateProfileState();
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
  }

  logout() {
    this.auth.logout();
    this.isSeller = false;
    this.showProfile = false;
  }
}
