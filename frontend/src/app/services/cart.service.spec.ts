import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CartService } from './cart.service';
import { BASE_URL } from '../constants/constants';
import { CartResponseDTO, CartItemRequestDTO, CartItemUpdateDTO, CartUpdateRequest, CartStatus } from '../models/cart.model';

describe('CartService', () => {
  let service: CartService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CartService]
    });
    service = TestBed.inject(CartService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should load cart and update subject', () => {
    const mockCart: CartResponseDTO = { id: '1', items: [], totalPrice: 0, expiryTime: '2026-01-01' };

    service.loadCart();

    const req = httpMock.expectOne(`${BASE_URL}/api/cart`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCart);

    service.cart$.subscribe(cart => {
      expect(cart).toEqual(mockCart);
    });
  });

  it('should add item to cart', () => {
    const newItem: CartItemRequestDTO = { productId: '123', quantity: 1 };
    const mockResponse: CartResponseDTO = {
      id: '1',
      items: [{ productId: '123', quantity: 1, price: 10, productName: 'Test', total: 10 }],
      totalPrice: 10,
      expiryTime: '2026-01-01'
    };

    service.addToCart(newItem).subscribe(cart => {
      expect(cart).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${BASE_URL}/api/cart`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newItem);
    req.flush(mockResponse);
  });

  it('should get cart directly', () => {
    const mockCart: CartResponseDTO = { id: '1', items: [], totalPrice: 0, expiryTime: '2026-01-01' };

    service.getCart().subscribe(cart => {
      expect(cart).toEqual(mockCart);
    });

    const req = httpMock.expectOne(`${BASE_URL}/api/cart`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCart);
  });

  it('should update cart item', () => {
    const productId = '123';
    const update: CartItemUpdateDTO = { quantity: 5 };
    const mockResponse: CartResponseDTO = { id: '1', items: [], totalPrice: 50, expiryTime: '2026-01-01' };

    service.updateCartItem(productId, update).subscribe(cart => {
      expect(cart).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${BASE_URL}/api/cart/${productId}`);
    expect(req.request.method).toBe('PUT');
    // FormData is hard to inspect exactly, but we verify the call happened
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush(mockResponse);
  });

  it('should reorder items', () => {
    const orderId = 'order-123';
    const mockResponse: CartResponseDTO = { id: '2', items: [], totalPrice: 100, expiryTime: '2026-01-01' };

    service.reorderItems(orderId).subscribe(cart => {
      expect(cart).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${BASE_URL}/api/cart/reorder/${orderId}`);
    expect(req.request.method).toBe('POST');
    req.flush(mockResponse);
  });

  it('should delete item by id and reload cart', () => {
    const productId = '123';
    const mockCart: CartResponseDTO = { id: '1', items: [], totalPrice: 0, expiryTime: '2026-01-01' };

    service.deleteItemById(productId).subscribe(() => {
      // After delete completes, we check if loadCart triggered a GET
      const reqGet = httpMock.expectOne(`${BASE_URL}/api/cart`);
      expect(reqGet.request.method).toBe('GET');
      reqGet.flush(mockCart);
    });

    const reqDelete = httpMock.expectOne(`${BASE_URL}/api/cart/${productId}`);
    expect(reqDelete.request.method).toBe('DELETE');
    reqDelete.flush(null);
  });

  it('should delete entire cart and clear subject', () => {
    service.deleteCart().subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/api/cart/all`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    service.cart$.subscribe(cart => {
      expect(cart).toBeNull();
    });
  });

  it('should update cart status', () => {
    const statusUpdate: CartUpdateRequest = { cartStatus: CartStatus.CHECKOUT };

    service.updateCartStatus(statusUpdate).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/api/cart/status`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(statusUpdate);
    req.flush(null);
  });

  it('should clear cart manually', () => {
    service.clearCart();
    service.cart$.subscribe(cart => {
      expect(cart).toBeNull();
    });
  });
});
