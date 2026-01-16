// shopping cart item
export interface ItemDTO {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
  total: number;
  updating?: boolean;
}

// GET request to retrieve cart details
export interface CartResponseDTO {
  id: string;
  items: ItemDTO[];
  totalPrice: number;
  expiryTime: string;
}

// POST request to add item to cart
export interface CartItemRequestDTO {
  productId: string;
  quantity: number;
}

// PUT request to update item quantity in cart
export interface CartItemUpdateDTO {
  quantity: number;
}

export interface CartUpdateRequest {
  cartStatus: CartStatus;
}

export enum CartStatus {
  ACTIVE = 'ACTIVE',
  CHECKOUT = 'CHECKOUT',
  ABANDONED = 'ABANDONED'
}
