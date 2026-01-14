import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ProductViewComponent } from './product-view.component';
import { ProductService } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';
import { HttpClientTestingModule } from '@angular/common/http/testing';

const mockProduct: Product = {
  productId: '1',
  name: 'Test Product',
  price: 100,
  description: 'A great product',
  quantity: 10,
  userId: 'owner1'
};

describe('ProductViewComponent', () => {
  let component: ProductViewComponent;
  let fixture: ComponentFixture<ProductViewComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let activatedRoute: ActivatedRoute;

  const setupComponent = (productId: string | null) => {
    productServiceSpy = jasmine.createSpyObj('ProductService', ['getProductById']);
    authServiceSpy = jasmine.createSpyObj('AuthService', ['isLoggedIn']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [ProductViewComponent, HttpClientTestingModule],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => productId,
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProductViewComponent);
    component = fixture.componentInstance;
    activatedRoute = TestBed.inject(ActivatedRoute);
  };

  it('should create', () => {
    setupComponent('1');
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should fetch product and set login status when productId exists', fakeAsync(() => {
      setupComponent('1');
      productServiceSpy.getProductById.and.returnValue(of(mockProduct));
      authServiceSpy.isLoggedIn.and.returnValue(true);

      fixture.detectChanges();
      tick();

      expect(authServiceSpy.isLoggedIn).toHaveBeenCalled();
      expect(component.isLoggedIn).toBe(true);
      expect(productServiceSpy.getProductById).toHaveBeenCalledWith('1');
      expect(component.product).toEqual(mockProduct);
    }));

    it('should navigate to home if productId is null', () => {
      setupComponent(null);
      fixture.detectChanges();

      expect(routerSpy.navigate).toHaveBeenCalledWith(['']);
      expect(productServiceSpy.getProductById).not.toHaveBeenCalled();
    });

    it('should log an error if fetching product fails', () => {
      setupComponent('1');
      const consoleErrorSpy = spyOn(console, 'error');
      productServiceSpy.getProductById.and.returnValue(throwError(() => new Error('Fetch failed')));

      fixture.detectChanges();

      expect(productServiceSpy.getProductById).toHaveBeenCalledWith('1');
      expect(consoleErrorSpy).toHaveBeenCalled();
      expect(component.product).toBeNull();
    });
  });

  it('should navigate to update page when goToUpdateProduct is called', () => {
    setupComponent('1');
    const productId = 'test-product-id';
    component.goToUpdateProduct(productId);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/products/update', productId]);
  });
});
