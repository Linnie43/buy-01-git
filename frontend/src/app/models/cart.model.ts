// shopping cart item
export interface ItemDTO {
  productId: string;
  name: string;
  quantity: number;
  price: number;
  total: number;
}

// GET request to retrieve cart details
export interface CartResponseDTO {
  id: string;
  items: ItemDTO[];
  totalPrice: number;
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
