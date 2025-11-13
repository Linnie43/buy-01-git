import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { AvatarService } from '../../services/avatar.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.css']
})
export class AuthComponent {
  isLogin = true;
  selectedAvatar: File | null = null;

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
  }, { validators: (group) => {
      const p = group.get('password')?.value;
      const c = group.get('confirmPassword')?.value;
      return p && c && p !== c ? { passwordsMismatch: true } : null;
    }});

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private avatarService: AvatarService,
    private router: Router
  ) {}

  onAvatarSelected(e: Event) {
    const input = e.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedAvatar = input.files[0];
    }
  }

  onLoginSubmit() {
    if (!this.loginForm.valid) return;
    this.authService.login(this.loginForm.value).subscribe();
  }

  onSignupSubmit() {
    if (!this.signupForm.valid) return;

    const { email, password } = this.signupForm.value;
    this.authService.signup(this.signupForm.value).subscribe({
      next: () => {
        // auto login
        this.authService.loginNoReload({ email, password }).subscribe({
          next: () => {
            const userId = this.authService.getUserId();
            if (userId && this.selectedAvatar) {
              this.avatarService.uploadAvatar(this.selectedAvatar, userId).subscribe({
                next: () => window.location.reload(),
                error: () => window.location.reload()
              });
            } else {
              window.location.reload();
            }
          },
          error: () => this.isLogin = true
        });
      },
      error: () => {}
    });
  }
}
