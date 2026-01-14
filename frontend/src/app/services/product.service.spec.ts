import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProductService } from './product.service';
import { Product } from '../models/product.model';
import { PRODUCT_BASE_URL } from '../constants/constants';

describe('ProductService', () => {
  let service: ProductService;
  let httpTestingController: HttpTestingController;
  const apiUrl = `${PRODUCT_BASE_URL}`;

  const mockApiProduct: any = {
    productId: 'prod-1',
    name: 'Test Product',
    description: 'A great product',
    price: 99.99,
    quantity: 10,
    userId: 'user-1',
    images: ['image1.jpg'],
    isProductOwner: true
  };

  const mockMappedProduct: Product = {
    productId: 'prod-1',
    name: 'Test Product',
    description: 'A great product',
    price: 99.99,
    quantity: 10,
    userId: 'user-1',
    images: ['image1.jpg'],
    isProductOwner: true
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductService]
    });
    service = TestBed.inject(ProductService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAllProducts', () => {
    it('should return an array of mapped products', () => {
      service.getAllProducts().subscribe(products => {
        expect(products.length).toBe(1);
        expect(products[0]).toEqual(mockMappedProduct);
      });

      const req = httpTestingController.expectOne(apiUrl);
      expect(req.request.method).toBe('GET');
      req.flush([mockApiProduct]);
    });
  });

  describe('getProductById', () => {
    it('should return a single mapped product', () => {
      const productId = 'prod-1';
      service.getProductById(productId).subscribe(product => {
        expect(product).toEqual(mockMappedProduct);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/${productId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockApiProduct);
    });

    it('should map product with no images to use a placeholder', () => {
        const productId = 'prod-2';
        const productDataNoImages = { ...mockApiProduct, productId: 'prod-2', images: [] };

        service.getProductById(productId).subscribe(product => {
            expect(product.images).toEqual(['assets/product_image_placeholder.png']);
        });

        const req = httpTestingController.expectOne(`${apiUrl}/${productId}`);
        req.flush(productDataNoImages);
    });
  });

  describe('createProduct', () => {
    it('should POST data and return the new mapped product', () => {
      const formData = new FormData();
      formData.append('name', 'New Product');

      service.createProduct(formData).subscribe(product => {
        expect(product).toEqual(mockMappedProduct);
      });

      const req = httpTestingController.expectOne(apiUrl);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBe(formData);
      req.flush(mockApiProduct);
    });
  });

  describe('updateProduct', () => {
    it('should PUT data and return the updated mapped product', () => {
      const productId = 'prod-1';
      const formData = new FormData();
      formData.append('name', 'Updated Product');

      service.updateProduct(productId, formData).subscribe(product => {
        expect(product).toEqual(mockMappedProduct);
      });

      const req = httpTestingController.expectOne(`${apiUrl}/${productId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toBe(formData);
      req.flush(mockApiProduct);
    });
  });

  describe('deleteProduct', () => {
    it('should send a DELETE request to the correct URL', () => {
      const productId = 'prod-1';
      service.deleteProduct(productId).subscribe(() => {
      });

      const req = httpTestingController.expectOne(`${apiUrl}/${productId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null, { status: 204, statusText: 'No Content' });
    });
  });
});
