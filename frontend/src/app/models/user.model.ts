import { Product } from './product.model';
export interface User {
  name: string;
  email: string;
  role: string;
  avatar?: string;
  ownProfile: boolean;
  products: Product[];
}
