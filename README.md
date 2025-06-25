# RemotelyMap - Remote Work Place Finder

A full-stack application for finding and searching remote work-friendly places like cafes, coworking spaces, and libraries.

## Architecture

- **Backend**: Spring Boot API with PostGIS spatial database
- **Frontend**: Next.js React application with TypeScript
- **Database**: PostgreSQL with PostGIS extension for location-based queries

## Quick Start

### Prerequisites
- Docker and Docker Compose
- Git

### Running the Full Application

1. **Clone and navigate to the project:**
   ```bash
   git clone <repository-url>
   cd remotely-map
   ```

2. **Start all services:**
   ```bash
   docker-compose up --build
   ```
   This will automatically:
   - Build the backend and frontend Docker images
   - Start PostgreSQL with PostGIS extension
   - Run database migrations (Flyway)
   - Insert sample test data
   - Start all services

3. **Access the application:**
   - **Frontend**: http://localhost:3000
   - **Backend API**: http://localhost:8080
   - **API Documentation**: http://localhost:8080/swagger-ui.html
   - **Database**: localhost:5432 (user: `remotely`, password: `letmeinsecure`, db: `remotely`)

### Running Backend Only

If you only want to run the backend for API testing:

```bash
docker-compose up --build db backend
```

### Development Mode

For backend development with live reload:

1. **Start only the database:**
   ```bash
   docker-compose up db
   ```

2. **Run backend locally:**
   ```bash
   cd backend
   mvn spring-boot:run -Dspring-boot.run.profiles=test
   ```
   The test profile includes seed data for development.

## API Endpoints

### Core Endpoints
- `GET /api/places` - Browse all places with address/location data
- `GET /api/places/{id}` - Get specific place details
- `POST /api/places` - Create new place
- `PUT /api/places/{id}` - Update place
- `DELETE /api/places/{id}` - Delete place

### Search Endpoints
- `GET /api/places/nearby?lat={lat}&lng={lng}&radius={km}` - Find places within radius
- `GET /api/places/search?q={query}&city={city}` - Text search by name/description/city

### Example Usage
```bash
# Get all places
curl "http://localhost:8080/api/places"

# Find cafes within 2km of Central Park
curl "http://localhost:8080/api/places/nearby?lat=40.7829&lng=-73.9654&radius=2"

# Search for coffee shops in New York
curl "http://localhost:8080/api/places/search?q=coffee&city=New%20York"
```

## Sample Data

The application comes with 7 sample remote work places:
- **New York**: Central Park Cafe, Empire State Coworking, Times Square Internet Cafe, Brooklyn Bridge View Library, Liberty Coffee Roasters
- **San Francisco**: SF Remote Work Hub, Mission District Cafe

All places include:
- GPS coordinates for spatial queries
- Work-friendly descriptions (WiFi, power outlets, atmosphere)
- Real addresses and ratings

## Database Schema

The application uses three main tables:
- **places** - Place information and references to address/location
- **addresses** - Street addresses with city/country
- **locations** - PostGIS spatial data with latitude/longitude points
- **place_photos** - Photo URLs for places

## Technology Stack

### Backend
- Java 21
- Spring Boot 3.2.3
- PostgreSQL with PostGIS extension
- Flyway for database migrations
- MapStruct for object mapping
- Testcontainers for integration testing

### Frontend
- Next.js 15.3.3
- React 19
- TypeScript
- Tailwind CSS
- OpenAPI generated client

## Development Commands

### Backend
```bash
cd backend

# Compile
mvn compile

# Run tests
mvn test

# Run with test data
mvn spring-boot:run -Dspring-boot.run.profiles=test

# Build JAR
mvn clean package
```

### Frontend
```bash
cd frontend

# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build

# Generate API client (when backend is running)
npm run generate-api
```

## Environment Variables

### Backend Environment Variables
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username  
- `SPRING_DATASOURCE_PASSWORD` - Database password

### Frontend Environment Variables
- `API_URL` - Backend API base URL

## Troubleshooting

### Common Issues

1. **PostGIS extension not found**
   - Ensure you're using `postgis/postgis:16-3.4-alpine` image, not regular PostgreSQL

2. **Database connection refused**
   - Wait for PostgreSQL to fully start before backend
   - Check database credentials match between services

3. **Frontend can't reach backend**
   - Ensure backend is running on port 8080
   - Check `API_URL` environment variable in frontend

4. **Test failures with spatial data**
   - Verify PostGIS container is used in tests
   - Check that `asCompatibleSubstituteFor("postgres")` is configured

### Useful Commands

```bash
# View logs for specific service
docker-compose logs backend
docker-compose logs db

# Rebuild specific service
docker-compose up --build backend

# Access database directly
docker-compose exec db psql -U remotely -d remotely

# Stop all services
docker-compose down

# Clean up (removes volumes)
docker-compose down -v
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `mvn test` (backend) and `npm test` (frontend)
5. Submit a pull request

## License

This project is licensed under the MIT License.