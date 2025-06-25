'use client';

import { useState } from 'react';
import SearchBar from '@/components/SearchBar';
import PlaceCard from '@/components/PlaceCard';
import { useApi } from '@/lib/api/useApi';
import { placesApi } from '@/lib/api/placesApi';
import { PaginatedResponse, Place } from '@/lib/api/types';

export default function Home() {
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [searchType, setSearchType] = useState<'all' | 'text' | 'nearby'>('all');
  const [coordinates, setCoordinates] = useState<{ lat: number; lng: number } | null>(null);

  // Hook for getting all places
  const {
    data: allPlacesData,
    loading: allPlacesLoading,
    error: allPlacesError,
    execute: executeGetAllPlaces,
  } = useApi<PaginatedResponse<Place>, []>(() => placesApi.getAllPlaces());

  // Hook for text search
  const {
    data: searchPlacesData,
    loading: searchPlacesLoading,
    error: searchPlacesError,
    execute: executeSearchPlaces,
  } = useApi<PaginatedResponse<Place>, [string?, string?]>((query, city) => placesApi.searchPlaces(query, city));

  // Hook for nearby search
  const {
    data: nearbyPlacesData,
    loading: nearbyPlacesLoading,
    error: nearbyPlacesError,
    execute: executeNearbyPlaces,
  } = useApi<PaginatedResponse<Place>, [number, number, number?]>((lat, lng, radius) => placesApi.getPlacesNearby(lat, lng, radius));

  // Determine which data to show
  const placesData = searchType === 'text' ? searchPlacesData : 
                    searchType === 'nearby' ? nearbyPlacesData : 
                    allPlacesData;
  const loading = searchType === 'text' ? searchPlacesLoading :
                 searchType === 'nearby' ? nearbyPlacesLoading :
                 allPlacesLoading;
  const error = searchType === 'text' ? searchPlacesError :
               searchType === 'nearby' ? nearbyPlacesError :
               allPlacesError;

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    console.log('Searching for places with query:', query);
    
    // Check if the query looks like coordinates (lat,lng pattern)
    const coordsMatch = query.match(/^(-?\d+\.?\d*),\s*(-?\d+\.?\d*)$/);
    
    if (coordsMatch) {
      // If it's coordinates, use nearby search
      const lat = parseFloat(coordsMatch[1]);
      const lng = parseFloat(coordsMatch[2]);
      setCoordinates({ lat, lng });
      setSearchType('nearby');
      executeNearbyPlaces(lat, lng, 5.0);
    } else {
      // Use text search - could be city name or general search
      setSearchType('text');
      executeSearchPlaces(query, query); // Use query for both general search and city filter
    }
  };

  return (
    <main className="min-h-screen p-8">
      <div className="max-w-7xl mx-auto">
        <h1 className="text-4xl font-bold text-center mb-8">Find Places Near You</h1>
        
        <SearchBar onSearch={handleSearch} isLoading={loading} />

        {loading && (
          <div className="text-center mt-8">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-4 border-blue-600 border-t-transparent"></div>
            <p className="mt-2 text-gray-600">Loading places...</p>
          </div>
        )}

        {error && (
          <div className="mt-8 p-4 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-red-600">Error: {error.message}</p>
          </div>
        )}

        {placesData && (
          <div className="mt-8">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">
                {placesData.totalElements} places found
                {searchQuery && (
                  <span className="text-lg font-normal text-gray-600 ml-2">
                    {searchType === 'nearby' 
                      ? `near ${coordinates?.lat}, ${coordinates?.lng}` 
                      : `for "${searchQuery}"`}
                  </span>
                )}
              </h2>
              {searchQuery && (
                <button
                  onClick={() => {
                    setSearchQuery('');
                    setSearchType('all');
                    setCoordinates(null);
                    executeGetAllPlaces();
                  }}
                  className="px-4 py-2 text-sm text-blue-600 border border-blue-600 rounded-lg hover:bg-blue-50"
                >
                  Show All Places
                </button>
              )}
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {placesData.content?.map((place) => (
                <PlaceCard key={place.id} place={place} />
              ))}
            </div>
          </div>
        )}

        {!loading && !error && !placesData && (
          <div className="mt-8 text-center text-gray-600">
            <p>Enter a location to find places near you</p>
            <p className="text-sm mt-2">Try searching by city name or coordinates (lat,lng)</p>
          </div>
        )}
      </div>
    </main>
  );
}
