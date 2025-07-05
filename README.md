# GitHub Repository Searcher

A Spring Boot application that allows users to search for GitHub repositories using the GitHub REST API, stores search results in a PostgreSQL database, and provides API endpoints to retrieve stored results based on filter criteria.

## Features

- **GitHub Repository Search**: Search repositories by name, programming language, and sort by stars, forks, or updated date
- **PostgreSQL Storage**: Store repository details with upsert functionality (updates existing repositories)
- **Advanced Filtering**: Retrieve stored repositories with filters for language, minimum stars/forks, and name
- **Comprehensive Sorting**: Sort by stars, forks, updated date, or name in ascending/descending order
- **Robust Error Handling**: Proper validation and error messages for various scenarios
- **REST Compliant APIs**: Fully testable with Postman or any HTTP client

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Gradle 7.0 or higher
- GitHub API token (optional, for higher rate limits)

## Setup Instructions

### 1. Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE github_repo_searcher;
```

2. Create a user (optional):
```sql
CREATE USER github_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE github_repo_searcher TO github_user;
```

### 2. Application Configuration

**Important: Never commit sensitive credentials to Git!**

1. The main `application.properties` file contains placeholder values for security.

2. For local development, create `src/main/resources/application-local.properties` with your actual credentials:
```properties
# Local Development Configuration
# This file contains actual credentials and should NOT be committed to Git

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/githubsearch
spring.datasource.username=suraj
spring.datasource.password=suraj123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# GitHub API Configuration
# Replace with your actual GitHub Personal Access Token
github.api.token=your_actual_github_token_here

# Server Configuration
server.port=8080
```

3. Replace the placeholder values with your actual database credentials and GitHub token.

### 3. Running the Application

1. Clone the repository:
```bash
git clone <repository-url>
cd githubRepoSearch
```

2. Run the application with local profile:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Or run the JAR file:
```bash
./gradlew build
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

The application will start on `http://localhost:8080`

### 4. Environment Variables (Alternative)

Instead of using `application-local.properties`, you can use environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/githubsearch
export SPRING_DATASOURCE_USERNAME=suraj
export SPRING_DATASOURCE_PASSWORD=suraj123
export GITHUB_API_TOKEN=your_actual_github_token_here
./gradlew bootRun
```

## Security Best Practices

1. **Never commit credentials**: The `application-local.properties` file is already in `.gitignore`
2. **Use environment variables**: For production deployments, use environment variables instead of properties files
3. **GitHub Token**: Create a Personal Access Token with minimal required permissions (public_repo scope is sufficient)
4. **Database Security**: Use strong passwords and consider using connection pooling in production

## API Documentation

### 1. Search GitHub Repositories

**Endpoint:** `POST /api/github/search`

**Request Body:**
```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

**Parameters:**
- `query` (required): Search term for repository name
- `language` (optional): Programming language filter
- `sort` (optional): Sort by "stars", "forks", or "updated" (default: "stars")

**Response:**
```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

### 2. Retrieve Stored Repositories

**Endpoint:** `GET /api/github/repositories`

**Query Parameters:**
- `language` (optional): Filter by programming language
- `minStars` (optional): Minimum stars count
- `minForks` (optional): Minimum forks count
- `name` (optional): Filter by repository name (case-insensitive partial match)
- `sortBy` (optional): Sort by "stars", "forks", "updated", or "name" (default: "stars")
- `sortOrder` (optional): "asc" or "desc" (default: "desc")

**Example Request:**
```
GET /api/github/repositories?language=Java&minStars=100&sortBy=stars&sortOrder=desc
```

**Response:**
```json
{
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

## Testing with Postman

### 1. Search Repositories
1. Create a new POST request to `http://localhost:8080/api/github/search`
2. Set Content-Type header to `application/json`
3. Add request body:
```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```
4. Send the request

### 2. Retrieve Repositories
1. Create a new GET request to `http://localhost:8080/api/github/repositories`
2. Add query parameters:
   - `language=Java`
   - `minStars=100`
   - `sortBy=stars`
   - `sortOrder=desc`
3. Send the request

## Error Handling

The application provides comprehensive error handling:

- **400 Bad Request**: Invalid parameters (empty query, negative values, invalid sort options)
- **429 Too Many Requests**: GitHub API rate limit exceeded
- **503 Service Unavailable**: Network connectivity issues
- **500 Internal Server Error**: Unexpected errors

Error responses follow this format:
```json
{
  "error": "Error Type",
  "message": "Detailed error message"
}
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── controller/
│   │   │   └── GitHubRepositoryController.java
│   │   ├── service/
│   │   │   └── GitHubRepositoryService.java
│   │   ├── repository/
│   │   │   └── RepositoryRepository.java
│   │   ├── entity/
│   │   │   └── RepositoryEntity.java
│   │   ├── dto/
│   │   │   └── GitHubSearchRequest.java
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java
│   │   └── DemoApplication.java
│   └── resources/
│       ├── application.properties
│       └── application-local.properties (not committed to Git)
```

## Design Patterns and Best Practices

- **Layered Architecture**: Clear separation between controller, service, and repository layers
- **Dependency Injection**: Using Spring's IoC container
- **Repository Pattern**: Data access abstraction
- **Builder Pattern**: Entity creation with Lombok
- **Global Exception Handling**: Centralized error handling
- **Comprehensive Validation**: Input validation at service layer
- **Security**: Proper credential management and environment-specific configuration

## Rate Limiting

- Without GitHub token: 60 requests per hour
- With GitHub token: 5000 requests per hour

To use a GitHub token:
1. Go to GitHub Settings → Developer settings → Personal access tokens
2. Generate a new token with `public_repo` scope
3. Add it to your `application-local.properties` file

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Ensure no sensitive data is committed
5. Submit a pull request

## License

This project is licensed under the MIT License. 