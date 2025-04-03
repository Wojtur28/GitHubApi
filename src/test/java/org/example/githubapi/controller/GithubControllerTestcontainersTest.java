package org.example.githubapi.controller;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class GithubControllerTestcontainersTest {

    @LocalServerPort
    private int port;

    @Container
    static WireMockContainer wiremock = new WireMockContainer("wiremock/wiremock:3.3.1")
            .withMappingFromResource("mock", "stub-mock.json");

    @DynamicPropertySource
    static void overrideGithubApiUrl(DynamicPropertyRegistry registry) {
        registry.add("github.api-url", wiremock::getBaseUrl);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldReturnRepositoriesForUser() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "testUser")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("repo1"))
                .body("[0].branches", hasSize(2))
                .body("[0].branches.name", containsInAnyOrder("main", "feature"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "nonexistent")
                .then()
                .statusCode(404)
                .body("message", containsString("User not found"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenNoRepositoriesFound() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "empty")
                .then()
                .statusCode(500)
                .body("message", containsString("Repositories not found"));
    }

    @Test
    void shouldReturnForbiddenWhenRateLimitExceeded() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "ratelimit")
                .then()
                .statusCode(403)
                .body("message", containsString("API rate limit exceeded"));
    }

    @Test
    void shouldReturnInternalServerErrorWhenUnexpectedErrorOccurs() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "unexpected")
                .then()
                .statusCode(500)
                .body("message", containsString("Unexpected error"));
    }

    @Test
    void shouldReturnTooManyRequestsWhenRateLimitExceeded() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "throttle")
                .then()
                .statusCode(429)
                .body("message", containsString("Too many requests"));
    }

    @Test
    void shouldReturnRepositoryWithEmptyBranchesWhenNoBranchesAvailable() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "nobranches")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", equalTo("repo1"))
                .body("[0].branches", hasSize(0));
    }


    @Test
    void shouldReturnEmptyListWhenOnlyForksPresent() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/repositories/{username}", "forkonly")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }


}
