'use client';

import SearchBar from '@/components/SearchBar';
import PlaceCard from '@/components/PlaceCard';
import { useApi } from '@/lib/api/useApi';
import { placesApi } from '@/lib/api/placesApi';
import { PaginatedResponse, Place } from '@/lib/api/types';

export default function Home() {
  const {
    data: placesData,
    loading,
    error,
    execute: searchPlaces,
  } = useApi<PaginatedResponse<Place>, [number?, number?]>(placesApi.getAllPlaces);

  const handleSearch = (location: string) => {
    // TODO: Update this when backend supports location-based search
    console.log('Searching for places in:', location);
    searchPlaces(0, 10);
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
            <h2 className="text-2xl font-semibold mb-4">
              {placesData.totalElements} places found
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {placesData.content.map((place) => (
                <PlaceCard key={place.id} place={place} />
              ))}
            </div>
          </div>
        )}

        {!loading && !error && !placesData && (
          <div className="mt-8 text-center text-gray-600">
            <p>Enter a location to find places near you</p>
          </div>
        )}
      </div>
    </main>
  );
}
