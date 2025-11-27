import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';

const mockProducts: Product[] = [
  { productId: '1', name: 'Product A', price: 10, description: 'A', quantity: 5, ownerId: 'owner1', images: [] },
  { productId: '2', name: 'Product B', price: 20, description: 'B', quantity: 10, ownerId: 'owner2', images: [] },
];

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    productServiceSpy = jasmine.createSpyObj('ProductService', ['getAllProducts']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should fetch products and reverse them on initialization', fakeAsync(() => {
      productServiceSpy.getAllProducts.and.returnValue(of([...mockProducts]));
      authServiceSpy.isLoggedIn.and.returnValue(false);

      fixture.detectChanges();
      tick();

      expect(productServiceSpy.getAllProducts).toHaveBeenCalled();
      expect(component.products.length).toBe(2);
      expect(component.products[0].name).toBe('Product B'); // Check that the array was reversed
    }));

    it('should set isLoggedIn to true if the user is authenticated', () => {
      productServiceSpy.getAllProducts.and.returnValue(of([]));
      authServiceSpy.isLoggedIn.and.returnValue(true);

      fixture.detectChanges();

      expect(component.isLoggedIn).toBe(true);
    });

    it('should set isLoggedIn to false if the user is not authenticated', () => {
      productServiceSpy.getAllProducts.and.returnValue(of([]));
      authServiceSpy.isLoggedIn.and.returnValue(false);

      fixture.detectChanges();

      expect(component.isLoggedIn).toBe(false);
    });

    it('should log an error if fetching products fails', () => {
      const consoleErrorSpy = spyOn(console, 'error');
      productServiceSpy.getAllProducts.and.returnValue(throwError(() => new Error('Fetch failed')));

      fixture.detectChanges();

      expect(productServiceSpy.getAllProducts).toHaveBeenCalled();
      expect(consoleErrorSpy).toHaveBeenCalled();
      expect(component.products.length).toBe(0);
    });
  });

  it('should navigate to the product page when goToProduct is called', () => {
    const productId = 'test-id-123';
    component.goToProduct(productId);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/products', productId]);
  });
});
