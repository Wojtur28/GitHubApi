# GitHub Repository Viewer

Simple application to return a list of a user's repositories with branches using the GitHub API, excluding forks.

## Features

- List all non-fork GitHub repositories for a specified user.
- Retrieve branch names and last commit SHAs for each branch.
- Handle different error scenarios including user not found, API rate limit exceeded, and unexpected errors.

## Warning

Frequent API requests or requests for users with many repositories and extensive branches can quickly exhaust your GitHub API rate limit. Be mindful of this when using the application.

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven

### Installation

1. Clone the repository:

    ```sh
    git clone https://github.com/Wojtur28/GitHubApi
    cd GitHubApi
    ```

2. Build the project using Maven:

    ```sh
    mvn clean install
    ```

3. Run the application:

    ```sh
    mvn spring-boot:run
    ```

## API Endpoints

### List Repositories

**Request:**

- Method: `GET`
- URL: `/repositories/{username}`
- Headers:
    - `Accept: application/json`

**Response:**

- Status: `200 OK`
- Body:
  ```json
  [
    {
      "repositoryName": "repo1",
      "ownerLogin": {
        "login": "testuser"
      },
      "fork": false,
      "branches": [
        {
          "name": "main",
          "commit": {
            "sha": "abc123"
          }
        }
      ]
    }
  ]

- Status: `404 Not Found`
- Body:
  ```json
  [
    {
  "status": 404,
  "message": "User not found"
    }
  ]

- Status: `403 Forbidden`
- Body:
  ```json
  [
    {
  "status": 403,
  "message": "API rate limit exceeded. Please authenticate to get a higher rate limit."
    }
  ]

- Status: `500 Internal Server Error`
- Body:
  ```json
  [
    {
  "status": 500,
  "message": "An unexpected error occurred"
    }
  ]

## Using Swagger

You can use Swagger to send requests and explore the API.
After starting the application, navigate to the following URL to access the Swagger UI: \
http://localhost:8080/swagger-ui/index.html


## Running Tests

To run the tests, execute the following command:

```sh
mvn test
```
