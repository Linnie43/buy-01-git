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

  avatarFile: File | null = null;

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(3)]]
  });

  signupForm = this.fb.group({
      firstname: ['', [Validators.required, Validators.minLength(2)]],
      lastname: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(3)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(3)]],
      role: ['CLIENT', Validators.required]
    }, { validators: passwordsMatchValidator });

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {}

  toggleForm() {
    this.isLogin = !this.isLogin;
  }

  // some bug here? Not reliable when trying to log in with wrong credentials
  onLoginSubmit() {
    if (!this.loginForm.valid) return;

    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        console.log('Login success:', res);
      },
      error: (err) => {
        console.error('Login error:', err);
      }
    });
  }

  onSignupSubmit() {
    if (!this.signupForm.valid) return;

    console.log('Sending payload: ', this.signupForm.value);
    const formValue = this.signupForm.value;

    const formData = new FormData();
    formData.append('firstname', formValue.firstname!);
    formData.append('lastname', formValue.lastname!);
    formData.append('email', formValue.email!);
    formData.append('password', formValue.password!);
    formData.append('role', formValue.role!);

    // use the correct property name
    if (this.avatarFile) {
      formData.append('avatar', this.avatarFile);
    }

    this.authService.signup(formData).subscribe({
      next: (res) => {
        console.log('Signup success:', res);
        this.toggleForm(); // switch to login
      },
      error: (err) => {
        console.error('Signup error:', err);
      }
    });
  }

  onAvatarSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.avatarFile = file;
    }
  }
}
