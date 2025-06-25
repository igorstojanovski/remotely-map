import { 
  PlaceResponse, 
  PlaceRequest, 
  AddressResponse, 
  LocationResponse
} from './generated';

// Re-export generated types
export type { 
  PlaceResponse as Place, 
  PlaceRequest,
  AddressResponse,
  LocationResponse
};

// Extend generated types if needed
export interface PaginatedResponse<T> {
  content?: T[];
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
}

export type ApiError = {
  message: string;
  status: number;
  timestamp: string;
  path: string;
}; 