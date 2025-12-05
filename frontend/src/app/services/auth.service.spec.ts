import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { fakeAsync, tick } from '@angular/core/testing';
import { WINDOW } from '../window.token';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let routerSpy: any;
  let mockWindow: any;

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate'], { url: '/' });
    routerSpy.navigate.and.returnValue(Promise.resolve(true));

    mockWindow = { location: { reload: jasmine.createSpy('reload') } };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy },
        { provide: WINDOW, useValue: mockWindow } // Correctly use the imported token
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should clear token on logout', fakeAsync(() => {
    localStorage.setItem('token', 'dummy');
    service.logout();
    tick();  // advances microtasks so .then() runs
    expect(localStorage.getItem('token')).toBeNull();
    expect(mockWindow.location.reload).toHaveBeenCalled();
  }));


  it('should set and get avatar', () => {
    service.setAvatar('/avatar.png');
    expect(service.getAvatar()).toBe('/avatar.png');
  });

  it('should return null userId if no token', () => {
    expect(service.getUserId()).toBeNull();
  });

  it('should decode token on login', () => {
    const fakeToken = 'dummy.jwt.token';
    spyOn<any>(service as any, 'safeDecode').and.callThrough();
    service['safeDecode'](fakeToken);
    expect(service['safeDecode']).toHaveBeenCalledWith(fakeToken);
  });

  it('should return true when token is valid', () => {
    service['decodedToken'] = { exp: Date.now()/1000 + 3600, role: 'CLIENT' };
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('should return false when token expired', () => {
    service['decodedToken'] = { exp: Date.now()/1000 - 10, role: 'CLIENT' };
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('should call signup endpoint', () => {
    service.signup({ email: 'a', password: 'b' }).subscribe();
    const req = httpMock.expectOne(`${service['apiUrl']}/signup`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should call login endpoint, store token/avatar and reload', fakeAsync(() => {
    const fakeToken = 'header.payload.signature';
    service.login({ email: 'a', password: 'b' }).subscribe();
    const req = httpMock.expectOne(`${service['apiUrl']}/login`);
    expect(req.request.method).toBe('POST');

    req.flush({ token: fakeToken, avatar: '/avatar.png' });
    // advance microtasks to resolve router.navigate().then(...)
    tick();

    expect(localStorage.getItem('token')).toBe(fakeToken);
    expect(service.getAvatar()).toBe('/avatar.png');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
    expect(mockWindow.location.reload).toHaveBeenCalled();
  }));

  it('loginNoReload should store token without navigating', () => {
    const fakeToken = 'no.reload.token';
    service.loginNoReload({ email: 'a', password: 'b' }).subscribe();
    const req = httpMock.expectOne(`${service['apiUrl']}/login`);
    expect(req.request.method).toBe('POST');

    req.flush({ token: fakeToken });
    expect(localStorage.getItem('token')).toBe(fakeToken);
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('getCurrentUser should call users/me endpoint', () => {
    service.getCurrentUser().subscribe();
    const req = httpMock.expectOne((r) => r.url.endsWith('/api/users/me'));
    expect(req.request.method).toBe('GET');
    req.flush({ id: 'u1', email: 'a@b' });
  });

  it('getUserId should prefer userId, then id, then sub', () => {
    service['decodedToken'] = { userId: 'u-user', role: 'CLIENT', exp: Date.now()/1000 + 1000 };
    expect(service.getUserId()).toBe('u-user');

    service['decodedToken'] = { id: 'u-id', role: 'CLIENT', exp: Date.now()/1000 + 1000 };
    expect(service.getUserId()).toBe('u-id');

    service['decodedToken'] = { sub: 'u-sub', role: 'CLIENT', exp: Date.now()/1000 + 1000 };
    expect(service.getUserId()).toBe('u-sub');
  });

  it('safeDecode should not throw on invalid token and should clear decodedToken', () => {
    (service as any).safeDecode('not.a.valid.jwt');
    expect((service as any).decodedToken).toBeNull();
  });
});
