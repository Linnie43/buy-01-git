import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthComponent, passwordsMatchValidator } from './auth.component';
import { AuthService } from '../../services/auth.service';
import { ElementRef } from '@angular/core';
import { WINDOW } from '../../window.token'; // Import the token


describe('AuthComponent', () => {
  let component: AuthComponent;
  let fixture: ComponentFixture<AuthComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let mockWindow: any;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['login', 'signup']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    mockWindow = { location: { reload: jasmine.createSpy('reload') } };

    await TestBed.configureTestingModule({
      imports: [AuthComponent, ReactiveFormsModule, NoopAnimationsModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: WINDOW, useValue: mockWindow } // Provide the mock WINDOW token
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AuthComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize in login mode', () => {
    expect(component.isLogin).toBeTrue();
  });

  it('should toggle between login and signup forms', () => {
    component.toggleForm();
    expect(component.isLogin).toBeFalse();
    component.toggleForm();
    expect(component.isLogin).toBeTrue();
  });

  describe('Login Form', () => {
    it('should not call authService.login if form is invalid', () => {
      component.loginForm.setValue({ email: 'test', password: '12' }); // Invalid email and password
      component.onLoginSubmit();
      expect(authServiceSpy.login).not.toHaveBeenCalled();
    });

    it('should call authService.login on valid submission', () => {
      const loginData = { email: 'test@example.com', password: 'password123' };
      authServiceSpy.login.and.returnValue(of({}));
      component.loginForm.setValue(loginData);
      component.onLoginSubmit();
      expect(authServiceSpy.login).toHaveBeenCalledWith(loginData);
    });
  });

  describe('Signup Form', () => {
    it('should have passwordsMismatch error if passwords do not match', () => {
      component.signupForm.get('password')?.setValue('password123');
      component.signupForm.get('confirmPassword')?.setValue('password456');
      expect(component.signupForm.hasError('passwordsMismatch')).toBeTrue();
    });

    it('should not call authService.signup if form is invalid', () => {
      component.signupForm.setValue({
        firstname: 'a', // Invalid minlength
        lastname: 'b', // Invalid minlength
        email: 'test@example.com',
        password: 'password123',
        confirmPassword: 'password456', // Mismatch
        role: 'CLIENT'
      });
      component.onSignupSubmit();
      expect(authServiceSpy.signup).not.toHaveBeenCalled();
    });

    it('should call authService.signup for a CLIENT role on valid submission', () => {
      authServiceSpy.signup.and.returnValue(of({}));
      component.signupForm.setValue({
        firstname: 'Test',
        lastname: 'User',
        email: 'client@example.com',
        password: 'password123',
        confirmPassword: 'password123',
        role: 'CLIENT'
      });
      component.onSignupSubmit();
      expect(authServiceSpy.signup).toHaveBeenCalled();
      const formData = authServiceSpy.signup.calls.mostRecent().args[0] as FormData;
      expect(formData.get('role')).toBe('CLIENT');
      expect(formData.has('avatar')).toBeFalse();
    });

    it('should switch to login view on successful signup', () => {
      authServiceSpy.signup.and.returnValue(of({}));
      component.isLogin = false;
      component.signupForm.setValue({
        firstname: 'Test',
        lastname: 'User',
        email: 'new@example.com',
        password: 'password123',
        confirmPassword: 'password123',
        role: 'CLIENT'
      });
      component.onSignupSubmit();
      expect(component.isLogin).toBeTrue();
      expect(component.loginForm.get('email')?.value).toBe('new@example.com');
    });
  });

  describe('Avatar Handling', () => {
    beforeEach(() => {
      component.avatarInputRef = new ElementRef({ value: '' } as HTMLInputElement);
    });

    it('should set avatarError for invalid file type', () => {
      component.signupForm.get('role')?.setValue('SELLER');
      const blob = new Blob([''], { type: 'text/plain' });
      const file = new File([blob], 'test.txt', { type: 'text/plain' });
      const mockEvent = { target: { files: [file] } } as unknown as Event;

      component.onAvatarSelected(mockEvent);

      expect(component.avatarError).toContain('Invalid file type');
      expect(component.avatarFile).toBeNull();
    });

    it('should set avatarFile for a valid file when role is SELLER', () => {
      component.signupForm.get('role')?.setValue('SELLER');
      const blob = new Blob([''], { type: 'image/png' });
      const file = new File([blob], 'test.png', { type: 'image/png' });
      const mockEvent = { target: { files: [file] } } as unknown as Event;

      component.onAvatarSelected(mockEvent);

      expect(component.avatarError).toBeNull();
      expect(component.avatarFile).toBe(file);
    });

    it('should reset avatar when role changes from SELLER', () => {
      component.signupForm.get('role')?.setValue('SELLER');
      component.avatarFile = new File([], 'fake.jpg');
      component.avatarError = 'Some Error';

      component.signupForm.get('role')?.setValue('CLIENT');
      fixture.detectChanges();

      expect(component.avatarFile).toBeNull();
      expect(component.avatarError).toBeNull();
    });
  });
});
