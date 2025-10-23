import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  isLogin = true; // toggle: true = login, false = signup

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  signupForm = this.fb.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(3)]],
    role: ['client', Validators.required]
  });

  constructor(private fb: FormBuilder) {}

  toggleForm() {
    this.isLogin = !this.isLogin;
  }

  onLoginSubmit() {
    if (this.loginForm.valid) {
      console.log('Login:', this.loginForm.value);
      // backend login API call
    }
  }

  onSignupSubmit() {
    if (this.signupForm.valid) {
      console.log('Signup:', this.signupForm.value);
      // backend signup API call
    }
  }
}
