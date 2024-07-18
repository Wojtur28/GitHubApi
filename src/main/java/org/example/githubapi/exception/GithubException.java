package org.example.githubapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class GithubException extends RuntimeException {

    public enum FailReason {
        USER_NOT_FOUND,
        RATE_LIMIT_EXCEEDED,
        UNEXPECTED_ERROR
    }

    GithubException.FailReason failReason;

    private String message;
}

