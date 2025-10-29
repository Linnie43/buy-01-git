export interface Product {
  productId: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  sellerId?: string;
  image?: string;
}
