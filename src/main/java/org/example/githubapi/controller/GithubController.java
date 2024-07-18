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
        return switch (ex.getFailReason()) {
            case USER_NOT_FOUND ->
                    new ResponseEntity<>(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()), HttpStatus.NOT_FOUND);
            case RATE_LIMIT_EXCEEDED ->
                    new ResponseEntity<>(new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()), HttpStatus.FORBIDDEN);
            default ->
                    new ResponseEntity<>(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"), HttpStatus.INTERNAL_SERVER_ERROR);
        };
    }
}
