export interface ItemDTO {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
  sellerId: string;
}

export interface OrderResponseDTO {
  orderId: string;
  items: ItemDTO[];
  totalPrice: number;
  status: string;
  createdAt: string;
}

export interface OrderDashboardDTO {
  orders: OrderResponseDTO[];
  topItems: ItemDTO[];
  total: number;
}
