import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { OrderViewComponent } from './order-view.component';
import { OrderService } from '../../services/order.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { OrderResponseDTO, Status } from '../../models/order.model';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';

const mockOrder: OrderResponseDTO = {
  orderId: 'order-123',
  items: [{
    productId: 'prod-1',
    productName: 'Test Product',
    quantity: 2,
    price: 10,
    subtotal: 20,
    sellerId: 'seller-1'
  }],
  totalPrice: 20,
  status: Status.CREATED,
  shippingAddress: {
    fullName: 'Test User',
    street: '123 Test St',
    city: 'Testville',
    postalCode: '12345',
    country: 'Testland'
  },
  paid: true,
  deliveryDate: new Date(),
  trackingNumber: 'TRACK123',
  createdAt: new Date(),
  updatedAt: null
};

describe('OrderViewComponent', () => {
  let component: OrderViewComponent;
  let fixture: ComponentFixture<OrderViewComponent>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;
  let cartServiceSpy: jasmine.SpyObj<CartService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;
  let route: ActivatedRoute;

  beforeEach(async () => {
    orderServiceSpy = jasmine.createSpyObj('OrderService', ['getOrderById', 'cancelOrder']);
    cartServiceSpy = jasmine.createSpyObj('CartService', ['reorderItems']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn', 'getUserRole']);

    await TestBed.configureTestingModule({
      imports: [
        OrderViewComponent,
        RouterTestingModule.withRoutes([]),
        NoopAnimationsModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy },
        { provide: CartService, useValue: cartServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => 'order-123',
              },
            },
          },
        },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OrderViewComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    route = TestBed.inject(ActivatedRoute);
    spyOn(router, 'navigate').and.stub();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch order details on init', fakeAsync(() => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.getUserRole.and.returnValue('USER');
    orderServiceSpy.getOrderById.and.returnValue(of(mockOrder));

    fixture.detectChanges(); // ngOnInit() is called
    tick();

    expect(orderServiceSpy.getOrderById).toHaveBeenCalledWith('order-123');
    expect(component.order).toEqual(mockOrder);
    expect(component.isLoggedIn).toBe(true);
    expect(component.isSeller).toBe(false);
  }));

  it('should navigate to home if orderId is not present', () => {
    spyOn(route.snapshot.paramMap, 'get').and.returnValue(null);
    fixture.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['']);
  });

  it('should handle error when fetching order fails', () => {
    const consoleErrorSpy = spyOn(console, 'error');
    orderServiceSpy.getOrderById.and.returnValue(throwError(() => new Error('Fetch error')));
    fixture.detectChanges();
    expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching order:', jasmine.any(Error));
  });

  it('should call cancelOrder and update the order status', () => {
    const dialogRefSpyObj = jasmine.createSpyObj({ afterClosed: of(true), close: null });
    spyOn(component['dialog'], 'open').and.returnValue(dialogRefSpyObj);

    component.order = { ...mockOrder, orderId: 'order-123' };
    const cancelledOrder = { ...mockOrder, status: 'CANCELLED' as any };
    orderServiceSpy.cancelOrder.and.returnValue(of(cancelledOrder));

    component.cancelOrder();

    expect(orderServiceSpy.cancelOrder).toHaveBeenCalledWith('order-123');
    expect(component.order).toEqual(cancelledOrder);
  });

  it('should navigate to product page on goToProduct', () => {
    component.goToProduct('prod-1');
    expect(router.navigate).toHaveBeenCalledWith(['/products', 'prod-1']);
  });

  it('should call reorderItems and navigate to cart on confirmReorder', () => {
    component.order = { ...mockOrder };
    cartServiceSpy.reorderItems.and.returnValue(of({} as any)); // Mock CartResponseDTO

    component.confirmReorder();

    expect(cartServiceSpy.reorderItems).toHaveBeenCalledWith('order-123');
    expect(router.navigate).toHaveBeenCalledWith(['/cart']);
  });
});
