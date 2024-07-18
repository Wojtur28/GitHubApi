package org.example.githubapi.controller;

import lombok.AllArgsConstructor;
import org.example.githubapi.exception.ErrorResponse;
import org.example.githubapi.exception.GithubException;
import org.example.githubapi.model.GithubRepository;
import org.example.githubapi.service.GithubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@AllArgsConstructor
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/repositories/{username}")
    public ResponseEntity<?> getRepositories(@PathVariable String username) {
        try {
            List<GithubRepository> repositories = githubService.getRepositories(username);
            return ResponseEntity.ok(repositories);
        } catch (GithubException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(404, e.getMessage()));
        }
    }

    @ExceptionHandler(GithubException.class)
    public ResponseEntity<ErrorResponse> handleGithubException(GithubException ex) {
        HttpStatus status = switch (ex.getFailReason()) {
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case RATE_LIMIT_EXCEEDED -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return new ResponseEntity<>(new ErrorResponse(status.value(), ex.getMessage()), status);
    }
}
