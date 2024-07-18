package org.example.githubapi.model;

import java.util.List;

public record GithubRepository(String name, Owner owner, boolean fork, List<GithubBranch> branches) {

    public String ownerLogin() {
        return owner != null ? owner.login() : null;
    }

    public record Owner(String login) {}
}
