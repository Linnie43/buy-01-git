export interface Product {
  productId: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  ownerId: string;
  images?: string[];
  isProductOwner?: boolean;
}
