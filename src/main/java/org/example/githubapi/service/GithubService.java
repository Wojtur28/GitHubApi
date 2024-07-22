package org.example.githubapi.service;

import lombok.RequiredArgsConstructor;
import org.example.githubapi.exception.GithubException;
import org.example.githubapi.model.GithubBranch;
import org.example.githubapi.model.GithubRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubService {

    @Value("${github.api-url}")
    private String githubApiUrl;

    private final RestTemplate restTemplate;

    public List<GithubRepository> getRepositories(String username) {
        String url = githubApiUrl + "/users/" + username + "/repos";

        try {
            ResponseEntity<GithubRepository[]> response = restTemplate.getForEntity(url, GithubRepository[].class);

            GithubRepository[] repositories = response.getBody();

            if (repositories == null || repositories.length == 0) {
                throw new GithubException(GithubException.FailReason.REPOSITORY_NOT_FOUND, "Repositories not found");
            }

            List<GithubRepository> repositoryList = new ArrayList<>();
            for (GithubRepository repo : repositories) {
                if (repo.fork()) {
                    continue;
                }

                String repoName = repo.name();
                String ownerLogin = repo.ownerLogin();

                String branchesUrl = githubApiUrl + "/repos/" + ownerLogin + "/" + repoName + "/branches";
                ResponseEntity<GithubBranch[]> branchesResponse = restTemplate.getForEntity(branchesUrl, GithubBranch[].class);
                GithubBranch[] branches = branchesResponse.getBody();

                List<GithubBranch> branchList = new ArrayList<>();
                if (branches != null) {
                    for (GithubBranch branch : branches) {
                        branchList.add(new GithubBranch(branch.name(), branch.commit()));
                    }
                }

                repositoryList.add(new GithubRepository(repoName, repo.owner(), false, branchList));
            }

            return repositoryList;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new GithubException(GithubException.FailReason.USER_NOT_FOUND, "User not found");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN && e.getResponseBodyAsString().contains("API rate limit exceeded")) {
                throw new GithubException(GithubException.FailReason.RATE_LIMIT_EXCEEDED, "API rate limit exceeded. Please authenticate to get a higher rate limit.");
            } else {
                throw new GithubException(GithubException.FailReason.UNEXPECTED_ERROR, "An unexpected error occurred");
            }
        } catch (HttpServerErrorException e) {
            throw new GithubException(GithubException.FailReason.UNEXPECTED_ERROR, "An unexpected error occurred");
        }
    }

}