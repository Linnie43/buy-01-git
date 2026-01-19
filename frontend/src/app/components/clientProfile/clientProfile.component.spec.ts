import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClientProfileComponent } from './clientProfile.component';
import { AuthService } from '../../services/auth.service';
import { UserService, User } from '../../services/user.service'; // Import User from the service
import { of, throwError } from 'rxjs';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';

describe('ClientProfileComponent', () => {
  let component: ClientProfileComponent;
  let fixture: ComponentFixture<ClientProfileComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;

  // This mock now conforms to the User type from user.service.ts
  const mockUser: User = {
    name: 'Bob Client',
    email: 'bob@client.com',
    role: 'USER',
    avatar: 'assets/default.jpg',
    ownProfile: true,
    products: []
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getMe']);

    await TestBed.configureTestingModule({
      imports: [
        ClientProfileComponent,
        RouterTestingModule
      ],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientProfileComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('when user is logged in', () => {
    beforeEach(() => {
      authServiceSpy.isLoggedIn.and.returnValue(true);
      // The mockUser now correctly matches the expected return type of getMe()
      userServiceSpy.getMe.and.returnValue(of(mockUser));
      fixture.detectChanges();
    });

    it('should set isLoggedIn to true', () => {
      expect(component.isLoggedIn).toBeTrue();
    });

    it('should load and display user data on init', () => {
      expect(userServiceSpy.getMe).toHaveBeenCalled();
      expect(component.user).toEqual(mockUser);

      const h1 = fixture.debugElement.query(By.css('h1')).nativeElement;
      expect(h1.textContent).toContain(mockUser.name);
    });

    it('should have a link to the user dashboard', () => {
      const dashboardButton = fixture.debugElement.query(By.css('button[routerLink="/my-dashboard"]'));
      expect(dashboardButton).not.toBeNull();
    });
  });

  describe('when user service fails', () => {
    it('should log an error and not display user info', () => {
      authServiceSpy.isLoggedIn.and.returnValue(true);
      const consoleErrorSpy = spyOn(console, 'error');
      userServiceSpy.getMe.and.returnValue(throwError(() => new Error('Failed to fetch')));

      fixture.detectChanges();

      expect(userServiceSpy.getMe).toHaveBeenCalled();
      expect(consoleErrorSpy).toHaveBeenCalled();
      expect(component.user).toBeNull();
      const h1 = fixture.debugElement.query(By.css('h1'));
      expect(h1).toBeNull();
    });
  });

  describe('when user is not logged in', () => {
    it('should show loading message and not fetch user', () => {
      authServiceSpy.isLoggedIn.and.returnValue(false);
      fixture.detectChanges();

      expect(component.isLoggedIn).toBeFalse();
      expect(userServiceSpy.getMe).not.toHaveBeenCalled();
      expect(component.user).toBeNull();

      const loadingEl = fixture.debugElement.query(By.css('p')).nativeElement;
      expect(loadingEl.textContent).toContain('Loading user profile...');
    });
  });
});
