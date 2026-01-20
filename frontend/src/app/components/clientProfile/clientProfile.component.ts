import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-clientProfile',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './clientProfile.component.html',
  styleUrl: './clientProfile.component.css'
})
export class ClientProfileComponent implements OnInit {
  user: User | null = null;
  isLoggedIn = false;

  constructor(private readonly authService: AuthService, private readonly userService: UserService) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn?.() ?? false;

    if (this.isLoggedIn) {
      // getting the user details from the backend
      this.userService.getMe().subscribe({
        next: (data: User) => {
          this.user = data;
        },
        error: (err: unknown) => console.error(err)
      });
    }
  }
}
