import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ManageProductsComponent } from './manage-products.component';
import { ProductService } from '../../services/product.service';
import { UserService } from '../../services/user.service';
import { Product } from '../../models/product.model';
import { User } from '../../models/user.model';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';

const mockProduct: Product = {
  productId: 'prod-123',
  name: 'Test Product',
  description: 'A test description',
  price: 10,
  quantity: 5,
  ownerId: 'user-1',
  images: ['image1.jpg']
};

const mockUser: User = {
  name: 'tester',
  email: 'test@example.com',
  role: 'user',
  ownProfile: true,
  products: [mockProduct],
  avatar: 'avatar.jpg'
};

class MatDialogMock {
  open() {
    return {
      afterClosed: () => of(true) // Simulate user confirming the dialog
    };
  }
}

describe('ManageProductsComponent', () => {
  let component: ManageProductsComponent;
  let fixture: ComponentFixture<ManageProductsComponent>;
  let productServiceSpy: jasmine.SpyObj<ProductService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let dialog: MatDialog;

  const configureTestBed = (productId: string | null) => {
    productServiceSpy = jasmine.createSpyObj('ProductService', ['getProductById', 'createProduct', 'updateProduct', 'deleteProduct']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getMe']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [
        ManageProductsComponent,
        ReactiveFormsModule,
        HttpClientTestingModule,
        NoopAnimationsModule,
        MatDialogModule
      ],
      providers: [
        { provide: ProductService, useValue: productServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: MatDialog, useClass: MatDialogMock },
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of({ get: (key: string) => productId })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ManageProductsComponent);
    component = fixture.componentInstance;
    dialog = TestBed.inject(MatDialog);
    userServiceSpy.getMe.and.returnValue(of(mockUser as any));
  };

  describe('in Create Mode', () => {
    beforeEach(() => {
      configureTestBed(null);
      fixture.detectChanges();
    });

    it('should initialize in create mode and load user products', fakeAsync(() => {
      tick();
      expect(component.mode).toBe('create');
      expect(userServiceSpy.getMe).toHaveBeenCalled();
      expect(component.sellerProducts.length).toBe(1);
      expect(productServiceSpy.getProductById).not.toHaveBeenCalled();
    }));

    it('should call createProduct on valid form submission', () => {
      productServiceSpy.createProduct.and.returnValue(of(mockProduct));
      component.productForm.setValue({ name: 'New Product', description: 'Desc', price: 1, quantity: 1 });
      component.submit();
      expect(productServiceSpy.createProduct).toHaveBeenCalled();
    });
  });

  describe('in Update Mode', () => {
    beforeEach(() => {
      configureTestBed('prod-123');
      productServiceSpy.getProductById.and.returnValue(of(mockProduct));
      fixture.detectChanges();
    });

    it('should initialize in update mode, load product data, and load user products', fakeAsync(() => {
      tick();
      expect(component.mode).toBe('update');
      expect(productServiceSpy.getProductById).toHaveBeenCalledWith('prod-123');
      expect(component.productForm.value.name).toBe(mockProduct.name);
      expect(userServiceSpy.getMe).toHaveBeenCalled();
    }));

    it('should call updateProduct on valid form submission', () => {
      productServiceSpy.updateProduct.and.returnValue(of(mockProduct));
      component.productForm.setValue({ name: 'Updated Product Name', description: 'Desc', price: 1, quantity: 1 });
      component.submit();
      expect(productServiceSpy.updateProduct).toHaveBeenCalledWith('prod-123', jasmine.any(FormData));
    });

    it('should call deleteProduct when onDelete is confirmed', () => {
      spyOn(dialog, 'open').and.callThrough();
      productServiceSpy.deleteProduct.and.returnValue(of(undefined));
      component.onDelete();
      expect(dialog.open).toHaveBeenCalled();
      expect(productServiceSpy.deleteProduct).toHaveBeenCalledWith('prod-123');
    });
  });

  describe('General Functionality', () => {
    beforeEach(() => {
      configureTestBed(null);
      fixture.detectChanges();
    });

    it('should not submit an invalid form', () => {
      component.productForm.reset();
      component.submit();
      expect(productServiceSpy.createProduct).not.toHaveBeenCalled();
      expect(productServiceSpy.updateProduct).not.toHaveBeenCalled();
    });

    it('should navigate when edit is called', () => {
      component.edit(mockProduct);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/products/update', mockProduct.productId]);
    });
  });
});
