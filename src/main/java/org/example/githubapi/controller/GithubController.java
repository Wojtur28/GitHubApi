package org.example.githubapi.controller;

import lombok.RequiredArgsConstructor;
import org.example.githubapi.model.GithubRepository;
import org.example.githubapi.service.GithubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/repositories")
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/{username}")
    public ResponseEntity<List<GithubRepository>> getRepositories(@PathVariable String username) {
        List<GithubRepository> repositories = githubService.getRepositories(username);
        return ResponseEntity.ok(repositories);
    }
}
