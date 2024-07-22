package org.example.githubapi.controller;

import org.example.githubapi.exception.GithubException;
import org.example.githubapi.model.GithubBranch;
import org.example.githubapi.model.GithubRepository;
import org.example.githubapi.service.GithubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GithubController.class)
public class GithubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GithubService githubService;

    @Test
    void testGetRepositories_Success() throws Exception {
        String username = "testuser";
        GithubBranch.Commit commit = new GithubBranch.Commit("abc123");
        GithubBranch branch = new GithubBranch("main", commit);
        GithubRepository.Owner owner = new GithubRepository.Owner("testuser");
        GithubRepository repo = new GithubRepository("repo1", owner, false, Collections.singletonList(branch));

        when(githubService.getRepositories(username)).thenReturn(Collections.singletonList(repo));

        mockMvc.perform(get("/repositories/{username}", username)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("repo1"))
                .andExpect(jsonPath("$[0].owner.login").value("testuser"))
                .andExpect(jsonPath("$[0].branches[0].name").value("main"))
                .andExpect(jsonPath("$[0].branches[0].commit.sha").value("abc123"));
    }

    @Test
    void testGetRepositories_UserNotFound() throws Exception {
        String username = "nonexistentuser";

        when(githubService.getRepositories(username))
                .thenThrow(new GithubException(GithubException.FailReason.USER_NOT_FOUND, "User not found"));

        mockMvc.perform(get("/repositories/{username}", username)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void testGetRepositories_RateLimitExceeded() throws Exception {
        String username = "testuser";

        when(githubService.getRepositories(username))
                .thenThrow(new GithubException(GithubException.FailReason.RATE_LIMIT_EXCEEDED, "API rate limit exceeded. Please authenticate to get a higher rate limit."));

        mockMvc.perform(get("/repositories/{username}", username)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("API rate limit exceeded. Please authenticate to get a higher rate limit."));
    }

    @Test
    void testGetRepositories_UnexpectedError() throws Exception {
        String username = "testuser";

        when(githubService.getRepositories(username))
                .thenThrow(new GithubException(GithubException.FailReason.UNEXPECTED_ERROR, "An unexpected error occurred"));

        mockMvc.perform(get("/repositories/{username}", username)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
