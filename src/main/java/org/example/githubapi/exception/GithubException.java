package org.example.githubapi.exception;


import lombok.Getter;

@Getter
public class GithubException extends RuntimeException {

    public enum FailReason {
        USER_NOT_FOUND,
        RATE_LIMIT_EXCEEDED,
        UNEXPECTED_ERROR,
        REPOSITORY_NOT_FOUND,
        TOO_MANY_REQUESTS,
    }

    private final FailReason failReason;

    public GithubException(FailReason failReason, String message) {
        super(message);
        this.failReason = failReason;
    }
}
