package org.example.githubapi.model;


public record GithubBranch(String name, Commit commit) {
    public String lastCommitSha() {
        return commit != null ? commit.sha() : null;
    }

    public record Commit(String sha) {}
}



