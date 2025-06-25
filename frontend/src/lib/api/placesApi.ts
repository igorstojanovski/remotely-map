import { PlacesApi, Configuration, PlaceRequest } from './generated';
import { ApiError } from './types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

// Create a singleton instance of the API client
const api = new PlacesApi(
  new Configuration({
    basePath: API_BASE_URL,
    headers: {
      'Content-Type': 'application/json',
    },
  })
);

// Error handler to maintain compatibility with existing error format
const handleError = (error: unknown): ApiError => {
  if (error instanceof Error) {
    return {
      message: error.message,
      status: 500,
      timestamp: new Date().toISOString(),
      path: '',
    };
  }
  return {
    message: 'An unexpected error occurred',
    status: 500,
    timestamp: new Date().toISOString(),
    path: '',
  };
};

// Wrapper class to maintain the same interface as before
class PlacesApiClient {
  async getAllPlaces(page = 0, size = 10) {
    try {
      const response = await api.getAllPlaces({ page, size });
      return response;
    } catch (error) {
      throw handleError(error);
    }
  }

  async getPlaceById(id: string) {
    try {
      const response = await api.getPlaceById({ id });
      return response;
    } catch (error) {
      throw handleError(error);
    }
  }

  async createPlace(place: PlaceRequest) {
    try {
      const response = await api.createPlace({ placeRequest: place });
      return response;
    } catch (error) {
      throw handleError(error);
    }
  }

  async updatePlace(id: string, place: PlaceRequest) {
    try {
      const response = await api.updatePlace({ id, placeRequest: place });
      return response;
    } catch (error) {
      throw handleError(error);
    }
  }

  async deletePlace(id: string) {
    try {
      await api.deletePlace({ id });
    } catch (error) {
      throw handleError(error);
    }
  }

  async uploadPhoto(id: string, file: File) {
    try {
      const response = await api.uploadPhoto({ id, file });
      return response;
    } catch (error) {
      throw handleError(error);
    }
  }

  async searchPlaces(query?: string, city?: string, page = 0, size = 10) {
    try {
      const response = await api.searchPlaces({ q: query, city, page, size });
      return response;
    } catch (error) {
      throw handleError(error);
    }
  }

  async getPlacesNearby(lat: number, lng: number, radius = 5.0, page = 0, size = 10) {
    try {
      const response = await api.getPlacesNearby({ lat, lng, radius, page, size });
      return response;
    } catch (error) {
      throw handleError(error);
    }
  }
}

// Export a singleton instance
export const placesApi = new PlacesApiClient(); 