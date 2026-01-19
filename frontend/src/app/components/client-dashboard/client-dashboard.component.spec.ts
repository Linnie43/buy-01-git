import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { throwError, Subject } from 'rxjs';
import { ClientDashboardComponent } from './client-dashboard.component';
import { OrderService } from '../../services/order.service';
import { OrderDashboardDTO, OrderResponseDTO, Status } from '../../models/order.model';
import { RouterTestingModule } from '@angular/router/testing';
import { By } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

describe('ClientDashboardComponent', () => {
  let component: ClientDashboardComponent;
  let fixture: ComponentFixture<ClientDashboardComponent>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;
  let router: Router;
  let getOrdersSubject: Subject<OrderDashboardDTO>;

  const mockDashboardData: OrderDashboardDTO = {
    total: 550.75,
    topItems: [
      { productId: 'prod-1', productName: 'Laptop', quantity: 1, price: 450.50, subtotal: 450.50, sellerId: 'seller-1' },
      { productId: 'prod-2', productName: 'Mouse', quantity: 2, price: 50.125, subtotal: 100.25, sellerId: 'seller-2' }
    ],
    orders: [
      {
        orderId: 'order-1',
        createdAt: new Date('2023-10-26T10:00:00Z'),
        status: Status.DELIVERED,
        totalPrice: 450.50,
        items: [],
        shippingAddress: { fullName: 'Bob Client', street: '123 Main St', city: 'Anytown', postalCode: '12345', country: 'Country' },
        paid: true,
        deliveryDate: new Date('2023-10-30T10:00:00Z'),
        trackingNumber: 'TN12345',
        updatedAt: new Date('2023-10-26T10:00:00Z')
      },
      {
        orderId: 'order-2',
        createdAt: new Date('2023-10-27T11:00:00Z'),
        status: Status.SHIPPED,
        totalPrice: 100.25,
        items: [],
        shippingAddress: { fullName: 'Bob Client', street: '456 Side St', city: 'Otherville', postalCode: '54321', country: 'Country' },
        paid: true,
        deliveryDate: null,
        trackingNumber: 'TN67890',
        updatedAt: new Date('2023-10-27T11:00:00Z')
      }
    ] as OrderResponseDTO[]
  };

  const emptyDashboardData: OrderDashboardDTO = {
    total: 0,
    topItems: [],
    orders: []
  };

  beforeEach(async () => {
    getOrdersSubject = new Subject<OrderDashboardDTO>();
    orderServiceSpy = jasmine.createSpyObj('OrderService', ['getOrders']);
    orderServiceSpy.getOrders.and.returnValue(getOrdersSubject.asObservable());

    await TestBed.configureTestingModule({
      imports: [
        ClientDashboardComponent,
        RouterTestingModule.withRoutes([]),
        FormsModule,
        CommonModule
      ],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientDashboardComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  describe('on initialization', () => {
    it('should show loading state initially', fakeAsync(() => {
      // ngOnInit is called, component.isLoading is true
      fixture.detectChanges();
      expect(component.isLoading).toBe(true);

      // Check that the loading message is displayed
      const loadingEl = fixture.debugElement.query(By.css('.text-center p'));
      expect(loadingEl.nativeElement.textContent).toContain('Loading your dashboard...');

      // Simulate the observable emitting data
      getOrdersSubject.next(emptyDashboardData);
      tick();
      fixture.detectChanges();

      // Now, loading should be false and the loading element should be gone
      expect(component.isLoading).toBe(false);
      const loadingElAfter = fixture.debugElement.query(By.css('.text-center p'));
      expect(loadingElAfter).toBeNull();
    }));

    it('should fetch and display dashboard data on success', fakeAsync(() => {
      fixture.detectChanges(); // ngOnInit

      getOrdersSubject.next(mockDashboardData);
      tick();
      fixture.detectChanges();

      expect(component.isLoading).toBeFalse();
      expect(component.dashboardData).toEqual(mockDashboardData);
      expect(component.filteredOrders.length).toBe(2);

      const totalSpent = fixture.debugElement.query(By.css('.card-title')).nativeElement;
      expect(totalSpent.textContent).toContain('€550.75');

      const topItems = fixture.debugElement.queryAll(By.css('.list-group-item'));
      expect(topItems.length).toBe(2);
      expect(topItems[0].nativeElement.textContent).toContain('Laptop');
    }));

    it('should display an error message if fetching data fails', fakeAsync(() => {
      orderServiceSpy.getOrders.and.returnValue(throwError(() => new Error('Fetch error')));
      fixture.detectChanges(); // ngOnInit
      tick();
      fixture.detectChanges();

      expect(component.isLoading).toBeFalse();
      expect(component.errorMessage).toContain('Failed to load dashboard data');
      const errorEl = fixture.debugElement.query(By.css('.alert-danger'));
      expect(errorEl).toBeTruthy();
    }));
  });

  describe('filtering', () => {
    beforeEach(fakeAsync(() => {
      fixture.detectChanges();
      getOrdersSubject.next(mockDashboardData);
      tick();
      fixture.detectChanges();
    }));

    it('should filter orders by status', () => {
      component.statusFilter = 'SHIPPED';
      component.applyFilters();
      fixture.detectChanges();

      expect(component.filteredOrders.length).toBe(1);
      expect(component.filteredOrders[0].status).toBe(Status.SHIPPED);
    });

    it('should filter orders by date', () => {
      component.dateFilter = '2023-10-26';
      component.applyFilters();
      fixture.detectChanges();

      expect(component.filteredOrders.length).toBe(1);
      expect(component.filteredOrders[0].orderId).toBe('order-1');
    });

    it('should clear filters and show all orders', () => {
      component.statusFilter = 'DELIVERED';
      component.applyFilters();
      expect(component.filteredOrders.length).toBe(1);

      component.clearFilters();
      expect(component.statusFilter).toBe('');
      expect(component.dateFilter).toBe('');
      expect(component.filteredOrders.length).toBe(2);
    });
  });

  describe('navigation', () => {
    beforeEach(fakeAsync(() => {
        fixture.detectChanges();
        getOrdersSubject.next(mockDashboardData);
        tick();
        fixture.detectChanges();
      }));

    it('should navigate to product page on goToProduct()', () => {
      const navigateSpy = spyOn(router, 'navigate');
      const productId = 'prod-123';
      component.goToProduct(productId);
      expect(navigateSpy).toHaveBeenCalledWith(['/products', productId]);
    });

    it('should navigate to order details page on viewOrder()', () => {
      const navigateSpy = spyOn(router, 'navigate');
      const orderId = 'order-1';
      component.viewOrder(orderId);
      expect(navigateSpy).toHaveBeenCalledWith(['/order', orderId]);
    });
  });
});
