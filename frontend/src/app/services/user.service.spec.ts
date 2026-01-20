import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { UserService, User } from './user.service';
import { USER_BASE_URL } from '../constants/constants';
import { WINDOW } from '../window.token';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  const mockUser: User = {
    name: 'Test User',
    email: 'test@example.com',
    role: 'USER',
    avatar: 'https://example.com/avatar.png',
    ownProfile: true,
    products: []
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        UserService,
        { provide: WINDOW, useValue: window }
      ]
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get current user info via getMe', () => {
    service.getMe().subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${USER_BASE_URL}/me`);
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('should update user info via putMe', () => {
    service.putMe().subscribe(user => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${USER_BASE_URL}/me`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({});
    req.flush(mockUser);
  });
});
