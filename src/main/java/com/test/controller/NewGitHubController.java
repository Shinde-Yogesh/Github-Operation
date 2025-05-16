package com.test.controller;

import com.test.dto.GithubLoginRequest;
import com.test.model.GitHubRepo;
import com.test.model.GitHubUser;
import com.test.service.NewGitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/github")
public class NewGitHubController {

    @Autowired
    private NewGitHubService gitHubService;

    @PostMapping("/login")
    public ResponseEntity<?> loginToGithub(@RequestBody GithubLoginRequest request) {
        GitHubUser user = gitHubService.authenticateWithToken(request.getToken());

        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    //for the all repositories

    @PostMapping("/myrepos")
    public ResponseEntity<?> getAuthenticatedUserRepos(@RequestBody GithubLoginRequest request) {
        List<GitHubRepo> repos = gitHubService.getAuthenticatedUserRepositories(request.getToken());

        if (repos.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No repositories found or token invalid.");
        }

        return ResponseEntity.ok(repos);
    }
}
