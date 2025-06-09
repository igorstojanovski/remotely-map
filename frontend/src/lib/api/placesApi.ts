import axios, { AxiosError, AxiosInstance, AxiosResponse } from 'axios';
import { Place, CreatePlaceRequest, UpdatePlaceRequest, PaginatedResponse, ApiError } from './types';

const API_BASE_URL = 'http://localhost:8080/api';

class PlacesApi {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // Add response interceptor for error handling
    this.client.interceptors.response.use(
      (response: AxiosResponse) => response,
      (error: AxiosError<ApiError>) => {
        const apiError = error.response?.data || {
          message: 'An unexpected error occurred',
          status: error.response?.status || 500,
          timestamp: new Date().toISOString(),
          path: error.config?.url || '',
        };
        return Promise.reject(apiError);
      }
    );
  }

  async getAllPlaces(page = 0, size = 10): Promise<PaginatedResponse<Place>> {
    try {
      const response = await this.client.get<PaginatedResponse<Place>>('/places', {
        params: { page, size },
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getPlaceById(id: number): Promise<Place> {
    try {
      const response = await this.client.get<Place>(`/places/${id}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async createPlace(place: CreatePlaceRequest): Promise<Place> {
    try {
      const response = await this.client.post<Place>('/places', place);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async updatePlace(id: number, place: UpdatePlaceRequest): Promise<Place> {
    try {
      const response = await this.client.put<Place>(`/places/${id}`, place);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async deletePlace(id: number): Promise<void> {
    try {
      await this.client.delete(`/places/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  private handleError(error: unknown): ApiError {
    if (axios.isAxiosError<ApiError>(error)) {
      const apiError = error.response?.data;
      return apiError || {
        message: error.message,
        status: error.response?.status || 500,
        timestamp: new Date().toISOString(),
        path: error.config?.url || '',
      };
    }
    return {
      message: 'An unexpected error occurred',
      status: 500,
      timestamp: new Date().toISOString(),
      path: '',
    };
  }
}

// Export a singleton instance
export const placesApi = new PlacesApi(); 