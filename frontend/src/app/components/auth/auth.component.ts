import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

export function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password');
  const confirmPassword = control.get('confirmPassword');

  if (password && confirmPassword && password.value !== confirmPassword.value) {
    return { passwordsMismatch: true };
  }
  return null;
}

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
      firstname: ['', [Validators.required, Validators.minLength(2)]],
      lastname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(3)]],
      confirmPassword: ['', Validators.required],
      role: ['CLIENT', Validators.required]
    }, { validators: passwordsMatchValidator });

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {}

  toggleForm() {
    this.isLogin = !this.isLogin;
  }

  onLoginSubmit() {
    if (!this.loginForm.valid) return;

    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        console.log('Login success:', res);
        this.router.navigate(['/']); // redirect after login
        //does it need to be reload?       window.location.reload();
      },
      error: (err) => {
        console.error('Login error:', err);
      }
    });
  }

  onSignupSubmit() {
    if (!this.signupForm.valid) return;

    console.log('Sending payload: ', this.signupForm.value);
    this.authService.signup(this.signupForm.value).subscribe({
      next: (res) => {
        console.log('Signup success:', res);
        this.toggleForm(); // switch to login after signup
      },
      error: (err) => {
        console.error('Signup error:', err);
      }
    });
  }
}
