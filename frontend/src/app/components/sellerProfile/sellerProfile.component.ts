import { Component } from '@angular/core';
import { OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-sellerProfile',
  standalone: true,
  imports: [],
  templateUrl: './sellerProfile.component.html',
  styleUrl: './sellerProfile.component.css'
})
export class SellerProfileComponent implements OnInit {
    user: User | null = null;
  isLoggedIn = false;

  constructor(private authService: AuthService, private userService: UserService) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;

    // getting the products from the backend
    this.userService.getMe().subscribe({
      next: (data: User) => {
        this.user = data;
      },
      error: (err: unknown) => console.error(err)
    });
  }
}
