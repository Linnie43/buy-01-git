import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CartComponent } from './cart.component';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CartResponseDTO, ItemDTO } from '../../models/cart.model';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('CartComponent', () => {
  let component: CartComponent;
  let fixture: ComponentFixture<CartComponent>;
  let cartServiceSpy: jasmine.SpyObj<CartService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;
  let router: Router;

  const mockCartItem: ItemDTO = {
    productId: 'prod-1',
    productName: 'Test Product',
    quantity: 2,
    price: 10,
    total: 20
  };

  const mockCart: CartResponseDTO = {
    id: 'cart-123',
    items: [mockCartItem],
    totalPrice: 20,
    expiryTime: new Date(Date.now() + 15 * 60 * 1000).toISOString() // 15 minutes from now
  };

  beforeEach(async () => {
    cartServiceSpy = jasmine.createSpyObj('CartService', ['getCart', 'updateCartItem', 'deleteItemById']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        CartComponent,
        RouterTestingModule.withRoutes([]),
        NoopAnimationsModule
      ],
      providers: [
        { provide: CartService, useValue: cartServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CartComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('when user is logged in', () => {
    beforeEach(() => {
      authServiceSpy.isLoggedIn.and.returnValue(true);
    });

    it('should load cart on init', () => {
      cartServiceSpy.getCart.and.returnValue(of(mockCart));
      fixture.detectChanges(); // ngOnInit
      expect(cartServiceSpy.getCart).toHaveBeenCalled();
      expect(component.cart).toEqual(mockCart);
    });

    it('should display cart items', () => {
      cartServiceSpy.getCart.and.returnValue(of(mockCart));
      fixture.detectChanges();
      const itemElements = fixture.debugElement.queryAll(By.css('.card-body'));
      expect(itemElements.length).toBe(1);
      const totalEl = fixture.debugElement.query(By.css('.card.p-3 strong')).nativeElement;
      expect(totalEl.textContent).toContain('€20.00');
    });

    it('should display empty cart message if cart has no items', () => {
      const emptyCart = { ...mockCart, items: [], totalPrice: 0 };
      cartServiceSpy.getCart.and.returnValue(of(emptyCart));
      fixture.detectChanges();
      const emptyCartEl = fixture.debugElement.query(By.css('.text-center h4'));
      expect(emptyCartEl.nativeElement.textContent).toContain('Your cart is empty');
    });

    it('should update item quantity successfully', () => {
      cartServiceSpy.getCart.and.returnValue(of(mockCart));
      fixture.detectChanges();

      const updatedCart = { ...mockCart, items: [{ ...mockCartItem, quantity: 3, total: 30 }], totalPrice: 30 };
      cartServiceSpy.updateCartItem.and.returnValue(of(updatedCart));

      component.updateQuantity(mockCartItem, 3);
      fixture.detectChanges();

      expect(cartServiceSpy.updateCartItem).toHaveBeenCalledWith('prod-1', { quantity: 3 });
      expect(component.cart).toEqual(updatedCart);
    });

    it('should show snackbar on quantity update error', () => {
      cartServiceSpy.getCart.and.returnValue(of(mockCart));
      fixture.detectChanges();
      cartServiceSpy.updateCartItem.and.returnValue(throwError(() => new Error('Stock error')));

      component.updateQuantity(mockCartItem, 5);
      fixture.detectChanges();

      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'Cannot add more than available quantity\\!',
        'Close',
        jasmine.any(Object)
      );
    });

    it('should remove an item from the cart', () => {
      cartServiceSpy.getCart.and.returnValue(of(mockCart));
      fixture.detectChanges();

      const emptyCart = { ...mockCart, items: [], totalPrice: 0 };
      cartServiceSpy.deleteItemById.and.returnValue(of(undefined));
      cartServiceSpy.getCart.and.returnValue(of(emptyCart)); // For the subsequent loadCart call

      component.removeItem(mockCartItem);
      fixture.detectChanges();

      expect(cartServiceSpy.deleteItemById).toHaveBeenCalledWith('prod-1');
      expect(cartServiceSpy.getCart).toHaveBeenCalledTimes(2); // Initial load + reload after delete
      expect(component.cart).toEqual(emptyCart);
    });

    it('should navigate to checkout', () => {
      const navigateSpy = spyOn(router, 'navigate');
      component.proceedToCheckout();
      expect(navigateSpy).toHaveBeenCalledWith(['/checkout']);
    });

    it('should display and update the cart timer', fakeAsync(() => {
      const expiryTime = new Date(Date.now() + 5000).toISOString();
      const initialCart = { ...mockCart, expiryTime };
      const emptyCart = { ...mockCart, items: [], totalPrice: 0, expiryTime: '' };

      // First call to getCart returns the cart with a short expiry
      // Second call (after expiry) returns an empty cart
      cartServiceSpy.getCart.and.returnValues(of(initialCart), of(emptyCart));

      fixture.detectChanges(); // ngOnInit -> loadCart()

      expect(component.timeLeftText).toBe('00:05');
      tick(2000); // Advance time by 2 seconds
      fixture.detectChanges();
      expect(component.timeLeftText).toBe('00:03');

      tick(3000); // Expire the timer, which triggers loadCart() again
      fixture.detectChanges();

      expect(component.timeLeftText).toBe('00:00');
      expect(snackBarSpy.open).toHaveBeenCalledWith('Cart hold expired. Refreshing cart\\.', 'Close', jasmine.any(Object));
      expect(cartServiceSpy.getCart).toHaveBeenCalledTimes(2);

      // Cleanup timers
      component.ngOnDestroy();
    }));
  });

  describe('when user is not logged in', () => {
    it('should not load cart on init', () => {
      authServiceSpy.isLoggedIn.and.returnValue(false);
      fixture.detectChanges(); // ngOnInit
      expect(cartServiceSpy.getCart).not.toHaveBeenCalled();
      expect(component.cart).toBeNull();
      const emptyCartEl = fixture.debugElement.query(By.css('.text-center h4'));
      expect(emptyCartEl.nativeElement.textContent).toContain('Your cart is empty');
    });
  });
});
