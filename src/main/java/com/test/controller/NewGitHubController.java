package com.test.controller;

import com.test.dto.GithubLoginRequest;
import com.test.model.GitHubUser;
import com.test.service.NewGitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
}
