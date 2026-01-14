export interface Product {
  productId: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  userId: string;
  category?: Category;
  images?: string[];
  isProductOwner?: boolean;
}

export enum Category {
  ELECTRONICS = 'ELECTRONICS',
  FASHION = 'FASHION',
  HOME_APPLIANCES = 'HOME_APPLIANCES',
  BOOKS = 'BOOKS',
  TOYS = 'TOYS',
  SPORTS = 'SPORTS',
  BEAUTY = 'BEAUTY',
  AUTOMOTIVE = 'AUTOMOTIVE',
  GROCERY = 'GROCERY',
  HEALTH = 'HEALTH',
  OTHER = 'OTHER',
}
