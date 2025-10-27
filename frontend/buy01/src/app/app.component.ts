import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { AuthService } from './services/auth.service';
import { Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClientModule } from '@angular/common/http'; // <--- add this


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterLink, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
   userRole = 'seller';
   profileImageUrl: string | null = null;
   isLoggedIn = false;
   showProfile = false;

  constructor(private auth: AuthService, private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.isLoggedIn = this.auth.isLoggedIn();
        this.showProfile = this.isLoggedIn && !event.url.includes('auth');
      }
    });
  }

 logout() {
   this.auth.logout();
   this.showProfile = false;
   }

}
