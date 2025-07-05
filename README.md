# GitHub Repository Searcher

This Spring Boot application lets you search for GitHub repositories, save the results in a PostgreSQL database, and provides endpoints to retrieve them with flexible filtering.

## Features

*   **GitHub Repository Search**: Find repositories by name, filter by language, and sort by stars, forks, or last updated date.
*   **PostgreSQL Storage**: Stores repository details, with the ability to update existing entries if new data is found.
*   **Advanced Filtering**: Retrieve stored repositories by language, minimum star count, minimum fork count, and name (partial match).
*   **Flexible Sorting**: Sort retrieved repositories by stars, forks, last updated date, or name, in ascending or descending order.
*   **Robust Error Handling**: Includes validation and informative error messages for various scenarios.
*   **REST Compliant APIs**: Designed to be easily tested with tools like Postman.

## Prerequisites

*   Java 17+
*   PostgreSQL 12+
*   Gradle 7.0+
*   GitHub API token (optional, but recommended for higher rate limits)

## Setup Instructions

### 1. Database Setup

1.  Create a PostgreSQL database:
    ```sql
    CREATE DATABASE github_repo_searcher;
    ```
2.  (Optional) Create a user and grant privileges:
    ```sql
    CREATE USER github_user WITH PASSWORD 'your_password';
    GRANT ALL PRIVILEGES ON DATABASE github_repo_searcher TO github_user;
    ```

### 2. Application Configuration

**Important Security Note:** Never commit sensitive credentials directly into your Git repository.

1.  The main `application.properties` file has placeholder values.
2.  For local development, create a `src/main/resources/application-local.properties` file and fill in your specific credentials:
    ```properties
    # Local Development Configuration
    # IMPORTANT: Do not commit this file to Git!

    # Database Connection Details
    spring.datasource.url=jdbc:postgresql://localhost:5432/githubsearch
    spring.datasource.username=suraj
    spring.datasource.password=suraj123
    spring.datasource.driver-class-name=org.postgresql.Driver

    # JPA and Hibernate Settings
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
    spring.jpa.properties.hibernate.format_sql=true

    # GitHub API Integration
    # Replace with your actual GitHub Personal Access Token
    github.api.token=your_actual_github_token_here

    # Server Port
    server.port=8080
    ```
3.  Replace the placeholder values with your actual database connection details and GitHub token.

### 3. Running the Application

1.  Clone the repository and navigate into the project directory:
    ```bash
    git clone <repository-url>
    cd githubRepoSearch
    ```
2.  Run the application using Gradle with the local profile activated:
    ```bash
    ./gradlew bootRun --args='--spring.profiles.active=local'
    ```
    Alternatively, build the JAR and run it:
    ```bash
    ./gradlew build
    java -jar build/libs/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
    ```
    The application will be accessible at `http://localhost:8080`.

### 4. Using Environment Variables (Alternative)

Instead of using `application-local.properties`, you can set your configurations via environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/githubsearch
export SPRING_DATASOURCE_USERNAME=suraj
export SPRING_DATASOURCE_PASSWORD=suraj123
export GITHUB_API_TOKEN=your_actual_github_token_here
./gradlew bootRun
```

## Security Best Practices

1.  **Credential Management**: Keep sensitive information out of your Git history. The `application-local.properties` file is already added to `.gitignore`.
2.  **Production Deployment**: For production environments, always use environment variables for credentials.
3.  **GitHub Token**: Generate a GitHub Personal Access Token with the minimal required permissions. The `public_repo` scope is generally sufficient.
4.  **Database Security**: Employ strong passwords for your database user and consider robust connection pooling for production loads.

## API Documentation

### 1. Search GitHub Repositories

*   **Endpoint:** `POST /api/github/search`
*   **Request Body:**
    ```json
    {
      "query": "spring boot",
      "language": "Java",
      "sort": "stars"
    }
    ```
*   **Parameters:**
    *   `query` (required): The search term for repository names.
    *   `language` (optional): Filter results by programming language.
    *   `sort` (optional): How to sort the results. Options: "stars", "forks", or "updated". Defaults to "stars".
*   **Successful Response Example:**
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

*   **Endpoint:** `GET /api/github/repositories`
*   **Query Parameters:**
    *   `language` (optional): Filter by programming language.
    *   `minStars` (optional): Minimum number of stars.
    *   `minForks` (optional): Minimum number of forks.
    *   `name` (optional): Filter by repository name (case-insensitive partial match).
    *   `sortBy` (optional): Field to sort by: "stars", "forks", "updated", or "name". Defaults to "stars".
    *   `sortOrder` (optional): Order of sorting: "asc" or "desc". Defaults to "desc".
*   **Example Request:**
    ```
    GET /api/github/repositories?language=Java&minStars=100&sortBy=stars&sortOrder=desc
    ```
*   **Successful Response Example:**
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

1.  Make a `POST` request to `http://localhost:8080/api/github/search`.
2.  Set the `Content-Type` header to `application/json`.
3.  Include the following JSON in the request body:
    ```json
    {
      "query": "spring boot",
      "language": "Java",
      "sort": "stars"
    }
    ```
4.  Send the request.

### 2. Retrieve Repositories

1.  Make a `GET` request to `http://localhost:8080/api/github/repositories`.
2.  Add the following query parameters as needed:
    *   `language=Java`
    *   `minStars=100`
    *   `sortBy=stars`
    *   `sortOrder=desc`
3.  Send the request.

## Error Handling

The application provides clear error responses for various situations:

*   **400 Bad Request**: For invalid input parameters (e.g., empty query, negative numeric values, unsupported sort options).
*   **429 Too Many Requests**: When the GitHub API rate limit is exceeded.
*   **503 Service Unavailable**: Indicates network connectivity problems.
*   **500 Internal Server Error**: For unexpected application errors.

Error responses will follow this structure:
```json
{
  "error": "Error Type",
  "message": "Detailed error message explaining the issue"
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
│       └── application-local.properties (excluded from Git)
```

## Design Patterns and Best Practices

*   **Layered Architecture**: Separates concerns into controller, service, and repository layers for maintainability.
*   **Dependency Injection**: Leverages Spring's IoC container for managing dependencies.
*   **Repository Pattern**: Provides an abstraction layer for data access operations.
*   **Builder Pattern**: Used with Lombok for cleaner entity object creation.
*   **Global Exception Handling**: Centralizes error management for consistent responses.
*   **Input Validation**: Implements comprehensive validation at the service layer to ensure data integrity.
*   **Security**: Emphasizes secure credential management and environment-specific configurations.

## Rate Limiting

*   **Without GitHub Token**: 60 requests per hour.
*   **With GitHub Token**: 5000 requests per hour.

To utilize a GitHub token:
1.  Navigate to GitHub Settings -> Developer settings -> Personal access tokens.
2.  Generate a new token and grant it the `public_repo` scope.
3.  Add this token to your `application-local.properties` file under the `github.api.token` property.
