import { TestBed } from '@angular/core/testing';
import { Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { RoleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('RoleGuard', () => {
  let guard: RoleGuard;
  let authService: jasmine.SpyObj<AuthService>;
  let router: jasmine.SpyObj<Router>;
  let route: ActivatedRouteSnapshot;
  let state: RouterStateSnapshot;

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getUserRole']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        RoleGuard,
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });

    guard = TestBed.inject(RoleGuard);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    route = { data: { role: 'SELLER' } } as any;
    state = {} as any;
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });

  it('should redirect an unauthenticated user to /', () => {
    authService.isLoggedIn.and.returnValue(false);
    const canActivate = guard.canActivate(route, state);
    expect(canActivate).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should redirect a user with the wrong role to /', () => {
    authService.isLoggedIn.and.returnValue(true);
    authService.getUserRole.and.returnValue('CLIENT');
    const canActivate = guard.canActivate(route, state);
    expect(canActivate).toBe(false);
    expect(router.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should allow a user with the correct role to access the route', () => {
    authService.isLoggedIn.and.returnValue(true);
    authService.getUserRole.and.returnValue('SELLER');
    const canActivate = guard.canActivate(route, state);
    expect(canActivate).toBe(true);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
