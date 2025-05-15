package com.test.controller;


import com.test.service.NewGitHubService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NewGitHubController {

    private final NewGitHubService gitHubService;

    public NewGitHubController(NewGitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/github/user")
    public String getUserInfo() {
        return gitHubService.getGitHubUserInfo();
    }
}
