package org.example.githubapi.model;


public record GithubBranch(String name, Commit commit) {

    public record Commit(String sha) {}
}



