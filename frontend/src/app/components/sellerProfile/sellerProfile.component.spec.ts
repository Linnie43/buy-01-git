import { TestBed, ComponentFixture } from '@angular/core/testing';
import { SellerProfileComponent } from './sellerProfile.component';
import { AuthService } from '../../services/auth.service';
import { UserService, User } from '../../services/user.service';
import { AvatarService } from '../../services/avatar.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

describe('SellerProfileComponent', () => {
  let component: SellerProfileComponent;
  let fixture: ComponentFixture<SellerProfileComponent>;

  let authServiceSpy: any;
  let userServiceSpy: any;
  let avatarServiceSpy: any;
  let routerSpy: any;
  let mockWindow: any;

  const mockUser: User = {
    name: 'Alice Seller',
    email: 'alice@seller.com',
    role: 'SELLER',
    avatar: 'avatar.png',
    ownProfile: true,
    products: [
      {
        productId: 'p1',
        name: 'Product 1',
        description: 'Description 1',
        price: 100,
        quantity: 5,
        ownerId: 'u1',
        images: ['img1.jpg'],
        isProductOwner: true
      },
      {
        productId: 'p2',
        name: 'Product 2',
        description: 'Description 2',
        price: 200,
        quantity: 2,
        ownerId: 'u1'
      }
    ]
  };

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getCurrentUser']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getMe']);
    avatarServiceSpy = jasmine.createSpyObj('AvatarService', ['updateMyAvatar', 'buildAvatarUrl']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    mockWindow = { location: { reload: jasmine.createSpy('reload') } };

    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.getCurrentUser.and.returnValue(of(mockUser));
    userServiceSpy.getMe.and.returnValue(of(mockUser));
    avatarServiceSpy.buildAvatarUrl.and.callFake((filename: string) => `url/${filename}`);
    avatarServiceSpy.updateMyAvatar.and.returnValue(of({ ...mockUser, avatar: 'new-avatar.png' }));

    await TestBed.configureTestingModule({
      imports: [SellerProfileComponent],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: AvatarService, useValue: avatarServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: 'WINDOW', useValue: mockWindow },
        { provide: ActivatedRoute, useValue: { snapshot: {}, paramMap: { get: () => null } } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SellerProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // initializes ngOnInit
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

   it('should load user data on init', () => {
     expect(component.user).toEqual(mockUser);
     expect(component.profileImageUrl)
       .toBe(`https://localhost:8443/api/media/avatar/${mockUser.avatar}`);
   });

  it('should navigate to product', () => {
    component.goToProduct('prod123');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/products', 'prod123']);
  });

  it('should update avatar when file selected', () => {
    const fakeFile = new File([''], 'avatar.png', { type: 'image/png' });
    const event = { target: { files: [fakeFile] } } as unknown as Event;

    component.onAvatarSelected(event);

    expect(avatarServiceSpy.updateMyAvatar).toHaveBeenCalledWith(fakeFile);
    expect(component.profileImageUrl).toBe('url/new-avatar.png');
    expect(component.user?.avatar).toBe('new-avatar.png');
    expect(mockWindow.location.reload).toHaveBeenCalled(); // now works
  });
});
