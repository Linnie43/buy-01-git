import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SalesDashboardComponent } from './sales-dashboard.component';
import { OrderService } from '../../services/order.service';
import { OrderDashboardDTO, Status } from '../../models/order.model';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

const mockDashboardData: OrderDashboardDTO = {
  orders: [
    {
      orderId: 'order-1',
      items: [{ productId: 'p1', productName: 'Item 1', quantity: 2, price: 10, subtotal: 20, sellerId: 's1' }],
      totalPrice: 20,
      status: Status.DELIVERED,
      createdAt: new Date('2023-10-26T10:00:00Z'),
      shippingAddress: {} as any, paid: true, deliveryDate: new Date(), trackingNumber: '123', updatedAt: null
    },
    {
      orderId: 'order-2',
      items: [{ productId: 'p2', productName: 'Item 2', quantity: 1, price: 30, subtotal: 30, sellerId: 's2' }],
      totalPrice: 30,
      status: Status.SHIPPED,
      createdAt: new Date('2023-10-27T11:00:00Z'),
      shippingAddress: {} as any, paid: true, deliveryDate: new Date(), trackingNumber: '456', updatedAt: null
    },
  ],
  topItems: [
    { productId: 'p1', productName: 'Item 1', quantity: 5, price: 10, subtotal: 50, sellerId: 's1' }
  ],
  total: 50,
};

describe('SalesDashboardComponent', () => {
  let component: SalesDashboardComponent;
  let fixture: ComponentFixture<SalesDashboardComponent>;
  let orderServiceSpy: jasmine.SpyObj<OrderService>;
  let router: Router;

  beforeEach(async () => {
    orderServiceSpy = jasmine.createSpyObj('OrderService', ['getOrders']);

    await TestBed.configureTestingModule({
      imports: [
        SalesDashboardComponent,
        RouterTestingModule,
        HttpClientTestingModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: OrderService, useValue: orderServiceSpy },
      ]
    })
    .overrideComponent(SalesDashboardComponent, {
      set: { template: '' } // Use an inline template to avoid parsing the external file
    })
    .compileComponents();

    fixture = TestBed.createComponent(SalesDashboardComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.stub();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load sales data on initialization', fakeAsync(() => {
    orderServiceSpy.getOrders.and.returnValue(of(mockDashboardData));

    fixture.detectChanges(); // ngOnInit()
    tick();

    expect(orderServiceSpy.getOrders).toHaveBeenCalled();
    expect(component.isLoading).toBeFalse();
    expect(component.errorMessage).toBeNull();
    expect(component.orders.length).toBe(2);
    expect(component.totalSales).toBe(50);
    expect(component.totalOrders).toBe(2);
    expect(component.totalUnitsSold).toBe(3); // 2 from order-1, 1 from order-2
    expect(component.topSellingItems.length).toBe(1);
    expect(component.filteredOrders.length).toBe(2);
  }));

  it('should handle error when loading sales data', fakeAsync(() => {
    orderServiceSpy.getOrders.and.returnValue(throwError(() => new Error('API Error')));

    fixture.detectChanges();
    tick();

    expect(component.isLoading).toBeFalse();
    expect(component.errorMessage).toBe('Failed to load sales data. Please try again later.');
    expect(component.orders.length).toBe(0);
  }));

  it('should filter orders by status', fakeAsync(() => {
    orderServiceSpy.getOrders.and.returnValue(of(mockDashboardData));
    fixture.detectChanges();
    tick();

    component.statusFilter = 'SHIPPED';
    component.applyFilters();

    expect(component.filteredOrders.length).toBe(1);
    expect(component.filteredOrders[0].status).toBe(Status.SHIPPED);
  }));

  it('should filter orders by date', fakeAsync(() => {
    orderServiceSpy.getOrders.and.returnValue(of(mockDashboardData));
    fixture.detectChanges();
    tick();

    component.dateFilter = '2023-10-26';
    component.applyFilters();

    expect(component.filteredOrders.length).toBe(1);
    expect(component.filteredOrders[0].orderId).toBe('order-1');
  }));

  it('should clear filters and show all orders', fakeAsync(() => {
    orderServiceSpy.getOrders.and.returnValue(of(mockDashboardData));
    fixture.detectChanges();
    tick();

    component.statusFilter = 'SHIPPED';
    component.dateFilter = '2023-10-26';
    component.applyFilters();
    expect(component.filteredOrders.length).toBe(0); // No order matches both

    component.clearFilters();
    expect(component.statusFilter).toBe('');
    expect(component.dateFilter).toBe('');
    expect(component.filteredOrders.length).toBe(2);
  }));

  it('should navigate to product page on goToProduct', () => {
    component.goToProduct('p1');
    expect(router.navigate).toHaveBeenCalledWith(['/products', 'p1']);
  });

  it('should navigate to order view on viewOrder', () => {
    component.viewOrder('order-1');
    expect(router.navigate).toHaveBeenCalledWith(['/order', 'order-1']);
  });
});
