export interface Place {
  id: number;
  name: string;
  description: string;
  address: string;
  photos: string[];
  createdAt: string;
  updatedAt: string;
  rating: number;
}

export interface CreatePlaceRequest {
  name: string;
  description: string;
  address: string;
  photos: string[];
}

export interface UpdatePlaceRequest {
  name?: string;
  description?: string;
  address?: string;
  photos?: string[];
  rating?: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export type ApiError = {
  message: string;
  status: number;
  timestamp: string;
  path: string;
}; 