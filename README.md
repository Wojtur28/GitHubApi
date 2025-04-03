# GitHub Repository Viewer

Simple Spring Boot application to return a list of a user's repositories with branches using the GitHub API, excluding forks.

## Features

- List all non-fork GitHub repositories for a specified user.
- Retrieve branch names and last commit SHAs for each branch.
- Handle various error scenarios:
  - User not found (`404`)
  - API rate limit exceeded (`403`)
  - Too many requests (`429`)
  - Unexpected errors (`500`)
- Uses `RestClient` for calling external APIs.
- Integration tests with `Testcontainers` and `WireMock`.

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

2. Build the project:

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

**Responses:**

- `200 OK`
  ```json
  [
    {
      "name": "repo1",
      "ownerLogin": "testuser",
      "fork": false,
      "branches": [
        {
          "name": "main",
          "lastCommitSha": "abc123"
        }
      ]
    }
  ]
