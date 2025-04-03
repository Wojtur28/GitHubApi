package org.example.githubapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.githubapi.exception.GithubException;
import org.example.githubapi.model.GithubBranch;
import org.example.githubapi.model.GithubRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubService {

    @Value("${github.api-url}")
    private String githubApiUrl;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public List<GithubRepository> getRepositories(String username) {
        String url = githubApiUrl + "/users/" + username + "/repos";

        try {
            String responseBody = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            List<GithubRepository> repositories = objectMapper.readValue(responseBody, new TypeReference<>() {});

            if (repositories == null || repositories.isEmpty()) {
                throw new GithubException(GithubException.FailReason.REPOSITORY_NOT_FOUND, "Repositories not found");
            }

            return repositories.stream()
                    .filter(repo -> !repo.fork())
                    .map(this::getBranches)
                    .toList();

        } catch (HttpClientErrorException.NotFound e) {
            throw new GithubException(GithubException.FailReason.USER_NOT_FOUND, "User not found");
        } catch (HttpClientErrorException.Forbidden e) {
            throw new GithubException(GithubException.FailReason.RATE_LIMIT_EXCEEDED, "API rate limit exceeded. Please authenticate.");
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new GithubException(GithubException.FailReason.TOO_MANY_REQUESTS, "Too many requests");
        } catch (Exception e) {
            if (e instanceof GithubException ge) {
                throw ge;
            }
            throw new GithubException(GithubException.FailReason.UNEXPECTED_ERROR, "Unexpected error");
        }

    }

    private GithubRepository getBranches(GithubRepository repo) {
        String branchesUrl = githubApiUrl + "/repos/" + repo.ownerLogin() + "/" + repo.name() + "/branches";

        try {
            String responseBody = restClient.get()
                    .uri(branchesUrl)
                    .retrieve()
                    .body(String.class);

            List<GithubBranch> branches = objectMapper.readValue(responseBody, new TypeReference<List<GithubBranch>>() {});
            return new GithubRepository(repo.name(), repo.owner(), false, branches);

        } catch (Exception e) {
            return new GithubRepository(repo.name(), repo.owner(), false, List.of());
        }
    }
}
