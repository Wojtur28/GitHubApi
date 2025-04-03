package org.example.githubapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.example.githubapi.exception.GithubException;
import org.example.githubapi.model.GithubRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;


import java.lang.reflect.Field;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GithubServiceTest {

    @InjectMocks
    private GithubService githubService;

    private static WireMockServer wireMockServer;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeAll
    static void setUpAll() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void tearDownAll() {
        wireMockServer.stop();
    }

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        wireMockServer.resetAll();
        RestClient restClient = RestClient.builder()
                .baseUrl(wireMockServer.baseUrl())
                .build();

        githubService = new GithubService(objectMapper, restClient);

        Field githubApiUrlField = GithubService.class.getDeclaredField("githubApiUrl");
        githubApiUrlField.setAccessible(true);
        githubApiUrlField.set(githubService, wireMockServer.baseUrl());
    }

    @Test
    void testGetRepositories_Success() {
        String username = "testuser";

        wireMockServer.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"repo1\",\"owner\":{\"login\":\"testuser\"},\"fork\":false}]")));

        wireMockServer.stubFor(get(urlEqualTo("/repos/testuser/repo1/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"main\",\"commit\":{\"sha\":\"abc123\"}}]")));

        List<GithubRepository> repositories = githubService.getRepositories(username);

        assertNotNull(repositories);
        assertEquals(1, repositories.size());
        assertEquals("repo1", repositories.getFirst().name());
        assertEquals("testuser", repositories.getFirst().ownerLogin());
        assertNotNull(repositories.getFirst().branches());
        assertEquals(1, repositories.getFirst().branches().size());
        assertEquals("main", repositories.getFirst().branches().getFirst().name());
        assertEquals("abc123", repositories.getFirst().branches().getFirst().lastCommitSha());
    }

    @Test
    void testGetRepositories_UserNotFound() {
        String username = "nonexistentuser";

        wireMockServer.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse().withStatus(404)));

        GithubException exception = assertThrows(GithubException.class, () -> githubService.getRepositories(username));

        assertEquals(GithubException.FailReason.USER_NOT_FOUND, exception.getFailReason());
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetRepositories_RateLimitExceeded() {
        String username = "testuser";

        wireMockServer.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse().withStatus(403).withBody("{\"message\":\"API rate limit exceeded\"}")));

        GithubException exception = assertThrows(GithubException.class, () -> githubService.getRepositories(username));

        assertEquals(GithubException.FailReason.RATE_LIMIT_EXCEEDED, exception.getFailReason());
        assertEquals("API rate limit exceeded. Please authenticate.", exception.getMessage());
    }

    @Test
    void testGetRepositories_UnexpectedError() {
        String username = "testuser";

        wireMockServer.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse().withStatus(500)));

        GithubException exception = assertThrows(GithubException.class, () -> githubService.getRepositories(username));

        assertEquals(GithubException.FailReason.UNEXPECTED_ERROR, exception.getFailReason());
        assertEquals("Unexpected error", exception.getMessage());
    }



    @Test
    void testGetRepositories_FilterForks() {
        String username = "testuser";

        wireMockServer.stubFor(get(urlEqualTo("/users/" + username + "/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"repo1\",\"owner\":{\"login\":\"testuser\"},\"fork\":true}]")));

        List<GithubRepository> repositories = githubService.getRepositories(username);

        assertNotNull(repositories);
        assertEquals(0, repositories.size());
    }
}

