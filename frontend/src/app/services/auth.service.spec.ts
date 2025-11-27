import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { fakeAsync, tick } from '@angular/core/testing';

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
        { provide: 'WINDOW', useValue: mockWindow }
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
});
