import { Place } from '@/lib/api/types';
import Link from 'next/link';
import Image from 'next/image';

interface PlaceCardProps {
  place: Place;
}

export default function PlaceCard({ place }: PlaceCardProps) {
  if (!place.id || !place.name) {
    return null;
  }

  const imageUrl = place.photos?.[0] || '/placeholder-image.jpg';
  const rating = place.rating?.toFixed(1) || 'N/A';
  
  // Handle both old and new address formats for backward compatibility
  const displayAddress = place.address 
    ? (typeof place.address === 'string' 
        ? place.address 
        : [place.address.street, place.address.city, place.address.country]
            .filter(Boolean)
            .join(', '))
    : 'No address available';

  return (
    <Link href={`/place/${place.id}`}>
      <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
        <div className="relative h-48 w-full">
          <Image
            src={imageUrl}
            alt={place.name}
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        </div>
        <div className="p-4">
          <h3 className="text-xl font-semibold text-gray-900 mb-2">{place.name}</h3>
          <p className="text-gray-600 mb-2 line-clamp-2">{place.description || 'No description available'}</p>
          <div className="flex items-center justify-between">
            <p className="text-sm text-gray-500">{displayAddress}</p>
            <div className="flex items-center">
              <span className="text-yellow-400">★</span>
              <span className="ml-1 text-gray-700">{rating}</span>
            </div>
          </div>
        </div>
      </div>
    </Link>
  );
} 