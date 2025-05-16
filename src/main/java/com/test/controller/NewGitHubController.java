package com.test.controller;

import com.test.dto.GithubLoginRequest;
import com.test.service.NewGitHubService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
public class NewGitHubController {

    private final NewGitHubService gitHubService;

    public NewGitHubController(NewGitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @PostMapping("/github/login")
    public ResponseEntity<String> loginToGithub(@RequestBody GithubLoginRequest request) {
        boolean success = gitHubService.authenticateWithToken(request.getToken());

        if (success) {
            return ResponseEntity.ok("Authentication successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }
}
