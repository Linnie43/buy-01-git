import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  ReactiveFormsModule,
  FormBuilder,
  Validators,
  AbstractControl,
  ValidationErrors,
  FormGroup
} from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

export function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password && confirm && password !== confirm ? { passwordsMismatch: true } : null;
}

export function noWhitespaceValidator(control: AbstractControl): ValidationErrors | null {
  if (control.value?.trim().length === 0) {
    return { whitespace: true };
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
export class AuthComponent implements OnInit {
  isLogin = true;
  avatarFile: File | null = null;
  avatarError: string | null = null;

  @ViewChild('avatarInput') avatarInputRef!: ElementRef<HTMLInputElement>;

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(3)]]
  });

  signupForm: FormGroup = this.fb.group(
    {
      firstname: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), noWhitespaceValidator]],
      lastname: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50), noWhitespaceValidator]],
      email: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/)
        ]
      ],
      password: ['', [Validators.required, Validators.minLength(3)]],
      confirmPassword: ['', [Validators.required]],
      role: ['CLIENT', [Validators.required]]
    },
    { validators: passwordsMatchValidator }
  );

  constructor(private readonly fb: FormBuilder, private readonly authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    // Reset avatar when switching role to CLIENT
    this.signupForm.get('role')?.valueChanges.subscribe(role => {
      if (role !== 'SELLER') {
        this.avatarFile = null;
        this.avatarError = null;
        if (this.avatarInputRef) {
          this.avatarInputRef.nativeElement.value = '';
        }
      }
    });
  }

  toggleForm(): void {
    this.isLogin = !this.isLogin;
  }

  onLoginSubmit(): void {
    if (this.loginForm.invalid) return;
    this.authService.login(this.loginForm.value).subscribe();
  }

  onSignupSubmit(): void {
    if (this.signupForm.invalid || this.avatarError) return;

    const formData = new FormData();
    formData.append('firstname', this.signupForm.get('firstname')?.value.trim());
    formData.append('lastname', this.signupForm.get('lastname')?.value.trim());
    formData.append('email', this.signupForm.get('email')?.value);
    formData.append('password', this.signupForm.get('password')?.value);
    formData.append('role', this.signupForm.get('role')?.value);

    if (this.signupForm.get('role')?.value === 'SELLER' && this.avatarFile) {
      formData.append('avatar', this.avatarFile);
    }

    this.authService.signup(formData).subscribe({
      next: () => {
        // After signup, switch to login view
        this.isLogin = true;
        this.loginForm.patchValue({ email: this.signupForm.get('email')?.value });
      },
      error: () => {}
    });
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    this.avatarError = null;
    this.avatarFile = null;

    if (!file) return;
    if (this.signupForm.get('role')?.value !== 'SELLER') return;

    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.avatarError = 'Invalid file type. Please select an image (jpeg, png, gif, webp).';
      this.avatarInputRef.nativeElement.value = '';
      return;
    }

    const maxSizeInBytes = 2 * 1024 * 1024; // 2MB
    if (file.size > maxSizeInBytes) {
      this.avatarError = 'File size exceeds 2MB.';
      this.avatarInputRef.nativeElement.value = '';
      return;
    }

    this.avatarFile = file;
  }
}
