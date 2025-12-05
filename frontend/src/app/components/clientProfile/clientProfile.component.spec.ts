import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ClientProfileComponent } from './clientProfile.component';
import { AuthService } from '../../services/auth.service';
import { UserService, User } from '../../services/user.service';
import { of } from 'rxjs';

describe('ClientProfileComponent', () => {
  let component: ClientProfileComponent;
  let fixture: ComponentFixture<ClientProfileComponent>;
  let authServiceSpy: any;
  let userServiceSpy: any;

  const mockUser: User = {
    name: 'Bob Client',
    email: 'bob@client.com',
    role: 'CLIENT',
    avatar: 'avatar.png',
    ownProfile: true,
    products: [] // clients don’t have products
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getMe']);

    authServiceSpy.isLoggedIn.and.returnValue(true);
    userServiceSpy.getMe.and.returnValue(of(mockUser));

    await TestBed.configureTestingModule({
      imports: [ClientProfileComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // triggers ngOnInit
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set isLoggedIn correctly', () => {
    expect(component.isLoggedIn).toBeTrue();
  });

  it('should load user data on init', () => {
    expect(component.user).toEqual(mockUser);
  });
});
