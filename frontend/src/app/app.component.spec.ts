import { TestBed, ComponentFixture, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { AuthService } from './services/auth.service';
import { RouterTestingModule } from '@angular/router/testing';
import { AVATAR_BASE_URL } from './constants/constants';
import { User as UserModel } from './models/user.model';
import { User as UserServiceUser } from './services/user.service';

// mock component for routing
import { Component } from '@angular/core';
@Component({ standalone: true, template: '' })
class MockAuthComponent {}
@Component({ standalone: true, template: '' })
class MockClientProfileComponent {}
@Component({ standalone: true, template: '' })
class MockSellerProfileComponent {}
@Component({ standalone: true, template: '' })
class MockDashboardComponent {}


describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;

  // sets mock user for testing
  const mockUser: UserModel = {
    name: 'Test User',
    email: 'test@example.com',
    role: 'CLIENT',
    avatar: 'avatar.png',
    ownProfile: true,
    products: [],
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getUserRole', 'getCurrentUser', 'logout']);

    await TestBed.configureTestingModule({
      imports: [
        AppComponent,
        RouterTestingModule.withRoutes([
          { path: 'auth', component: MockAuthComponent },
          { path: 'client-profile', component: MockClientProfileComponent },
          { path: 'seller-profile', component: MockSellerProfileComponent },
          { path: '**', component: MockDashboardComponent } // catch-all route
        ]),
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);

    // Default spy return values for a logged-out state
    authServiceSpy.isLoggedIn.and.returnValue(false);
    authServiceSpy.getUserRole.and.returnValue(null);
    authServiceSpy.getCurrentUser.and.returnValue(of(mockUser as UserServiceUser));

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it('should create the app', () => {
    expect(component).toBeTruthy();
  });

  describe('when user is not logged in', () => {
    it('should initialize with correct default values', fakeAsync(() => {
      fixture.detectChanges();
      tick(); // Complete async operations.

      expect(component.isLoggedIn).toBe(false);
      expect(component.isSeller).toBe(false);
      expect(component.profileRoute).toBe('/auth');
      expect(component.showProfile).toBe(false);
      expect(component.profileImageUrl).toBe('assets/default.jpg');
    }));
  });

  describe('when user is logged in as CLIENT', () => {
    it('should update state correctly', fakeAsync(() => {
      authServiceSpy.isLoggedIn.and.returnValue(true);
      authServiceSpy.getUserRole.and.returnValue('CLIENT');

      router.navigate(['/client-profile']);
      tick();
      fixture.detectChanges();

      expect(component.isLoggedIn).toBe(true);
      expect(component.isSeller).toBe(false);
      expect(component.profileRoute).toBe('/client-profile');
      expect(component.showProfile).toBe(true);
      expect(component.profileImageUrl).toBe(`${AVATAR_BASE_URL}/${mockUser.avatar}`);
    }));
  });

  describe('when user is logged in as SELLER', () => {
    it('should update state correctly', fakeAsync(() => {
      authServiceSpy.isLoggedIn.and.returnValue(true);
      authServiceSpy.getUserRole.and.returnValue('SELLER');

      router.navigate(['/seller-profile']);
      tick();
      fixture.detectChanges();

      expect(component.isLoggedIn).toBe(true);
      expect(component.isSeller).toBe(true);
      expect(component.profileRoute).toBe('/seller-profile');
      expect(component.showProfile).toBe(true);
      expect(component.profileImageUrl).toBe(`${AVATAR_BASE_URL}/${mockUser.avatar}`);
    }));
  });

  it('should update state on NavigationEnd event', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.getUserRole.and.returnValue('SELLER');

    router.navigate(['/new-page']); // test to simulate navigation
    tick();

    expect(component.isSeller).toBe(true);
    expect(component.profileRoute).toBe('/seller-profile');
  }));

  it('should call auth.logout() and reset state when logout() is called', () => {
    // Set initial logged-in state
    component.isLoggedIn = true;
    component.showProfile = true;
    component.isSeller = true;

    component.logout();

    expect(authServiceSpy.logout).toHaveBeenCalled();
    expect(component.showProfile).toBe(false);
    expect(component.isSeller).toBe(false);
  });
});
