import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheckoutComponent } from './checkout.component';
import { CartService } from '../../services/cart.service';
import { OrderService } from '../../services/order.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { CartStatus } from '../../models/cart.model';
import { OrderResponseDTO } from '../../models/order.model';

describe('CheckoutComponent', () => {
  let component: CheckoutComponent;
  let fixture: ComponentFixture<CheckoutComponent>;
  let cartServiceSpy: jasmine.SpyObj<CartService>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  const mockOrderResponse: OrderResponseDTO = {
    orderId: 'ORD-12345',
    deliveryDate: new Date(),
    status: 'CONFIRMED',
    items: [],
    totalPrice: 100.00,
    paid: false,
    trackingNumber: 'TRACK-999',
    createdAt: new Date(),
    updatedAt: new Date()
  } as unknown as OrderResponseDTO;

  beforeEach(async () => {
    cartServiceSpy = jasmine.createSpyObj('CartService', ['updateCartStatus']);
    // Default return value (so we don't get issues with ngDestroy)
    cartServiceSpy.updateCartStatus.and.returnValue(of(void 0));

    orderServiceSpy = jasmine.createSpyObj('OrderService', ['createOrder']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [CheckoutComponent, ReactiveFormsModule],
      providers: [
        { provide: CartService, useValue: cartServiceSpy },
        { provide: OrderService, useValue: orderServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CheckoutComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should update cart status to CHECKOUT on init', () => {
      cartServiceSpy.updateCartStatus.and.returnValue(of(void 0));
      fixture.detectChanges(); // triggers ngOnInit
      expect(cartServiceSpy.updateCartStatus).toHaveBeenCalledWith({ cartStatus: CartStatus.CHECKOUT });
    });
  });

  describe('Form Validation', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should be invalid when empty', () => {
      expect(component.checkoutForm.valid).toBeFalse();
    });

    it('should be valid when all fields are filled correctly', () => {
      component.checkoutForm.setValue({
        fullName: 'Test Client',
        street: '123 Test',
        city: 'Testville',
        postalCode: '12345',
        country: 'Testland',
        payOnDelivery: true
      });
      expect(component.checkoutForm.valid).toBeTrue();
    });

    it('should require payOnDelivery to be true', () => {
      component.checkoutForm.patchValue({
        fullName: 'Test Client',
        street: '123 Test',
        city: 'Testville',
        postalCode: '12345',
        country: 'Testland',
        payOnDelivery: false // Invalid
      });

      expect(component.checkoutForm.valid).toBeFalse();
      expect(component.checkoutForm.get('payOnDelivery')?.hasError('required')).toBeTrue();
    });
  });

  describe('onSubmit (Essentials)', () => {
    beforeEach(() => {
      fixture.detectChanges();

      component.checkoutForm.setValue({
        fullName: 'Test Client2',
        street: '123 Test',
        city: 'Testville',
        postalCode: '12345',
        country: 'Testland',
        payOnDelivery: true
      });
    });

    it('should call createOrder and show success snackbar', () => {
      orderServiceSpy.createOrder.and.returnValue(of(mockOrderResponse));

      component.onSubmit();

      expect(orderServiceSpy.createOrder).toHaveBeenCalledWith({
        shippingAddress: component.checkoutForm.value
      });
      expect(component.createdOrder).toEqual(mockOrderResponse);
      expect(snackBarSpy.open).toHaveBeenCalledWith(
        jasmine.stringMatching(/Order placed successfully/),
        'Close',
        jasmine.any(Object)
      );
    });

    it('should handle API errors', () => {
      orderServiceSpy.createOrder.and.returnValue(throwError(() => new Error('API Error')));
      const consoleSpy = spyOn(console, 'error');

      component.onSubmit();

      expect(orderServiceSpy.createOrder).toHaveBeenCalled();
      expect(component.createdOrder).toBeNull();
      expect(snackBarSpy.open).toHaveBeenCalledWith(
        jasmine.stringMatching(/Failed/),
        'Close',
        jasmine.any(Object)
      );
      expect(consoleSpy).toHaveBeenCalled();
    });

    it('should do nothing if form is invalid', () => {
      component.checkoutForm.patchValue({ payOnDelivery: false }); // Invalidate
      component.onSubmit();
      expect(orderServiceSpy.createOrder).not.toHaveBeenCalled();
    });
  });

  describe('ngOnDestroy test', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should revert cart status to ACTIVE if order was NOT placed', () => {
      component.createdOrder = null;
      cartServiceSpy.updateCartStatus.calls.reset(); // clear init call

      component.ngOnDestroy();

      expect(cartServiceSpy.updateCartStatus).toHaveBeenCalledWith({ cartStatus: CartStatus.ACTIVE });
    });

    it('should NOT revert cart status if order WAS placed', () => {
      component.createdOrder = mockOrderResponse;
      cartServiceSpy.updateCartStatus.calls.reset();

      component.ngOnDestroy();

      expect(cartServiceSpy.updateCartStatus).not.toHaveBeenCalled();
    });
  });

  describe('Navigation', () => {
    it('should navigate to home on continue shopping', () => {
      component.onContinueShopping();
      expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
    });
  });
});
