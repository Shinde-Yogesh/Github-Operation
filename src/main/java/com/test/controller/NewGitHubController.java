package com.test.controller;

import com.test.dto.GithubLoginRequest;
import com.test.model.GitHubRepo;
import com.test.model.GitHubUser;
import com.test.service.NewGitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

//    @PostMapping("/repo-structure")
//    public ResponseEntity<?> getRepoStructure(@RequestBody Map<String, String> request) {
//        String token = request.get("token");
//        String owner = request.get("owner");
//        String repo = request.get("repo");
//
//        Map<String, Object> structure = gitHubService.getRepositoryStructure(token, owner, repo);
//
//        if (structure == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Repository structure not found or invalid inputs.");
//        }
//
//        return ResponseEntity.ok(structure);
//    }

    //getting the actual path
    @PostMapping("/repo-paths")
    public ResponseEntity<?> getRepoPaths(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String owner = request.get("owner");
        String repo = request.get("repo");

        List<String> paths = gitHubService.getRepositoryFilePaths(token, owner, repo);

        if (paths == null || paths.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No files found or error fetching repository.");
        }

        return ResponseEntity.ok(paths);
    }

    //For getting the file Content
    //Program work for string also for Calling and checking the question and ans
    @PostMapping("/repo-file")
    public ResponseEntity<?> getFileContent(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String owner = request.get("owner");
        String repo = request.get("repo");
        String path = request.get("path");

        if (path == null || path.isEmpty()) {
            return ResponseEntity.badRequest().body("File path is required.");
        }

        String content = gitHubService.getFileContent(token, owner, repo, path);

        if (content == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found or cannot be read.");
        }

        return ResponseEntity.ok(content);
    }

    //for the return actual data type
    @PostMapping("/repo-file/raw")
    public ResponseEntity<?> getRawFile(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String owner = request.get("owner");
        String repo = request.get("repo");
        String path = request.get("path");

        try {
            // Fetch file metadata and content from GitHub
            byte[] fileContent = gitHubService.getFileAsBytes(token, owner, repo, path);
            if (fileContent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            String contentType = gitHubService.detectContentType(path);

            //checking the content type
            System.out.println("Here is the Content Type :- ");
            System.out.println(contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(fileContent);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    //for the getting the Issue as List

    @PostMapping("/github/issues")
    public ResponseEntity<?> getRepoIssues(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String owner = request.get("owner");
        String repo = request.get("repo");

        if (token == null || owner == null || repo == null) {
            return ResponseEntity.badRequest().body("Token, owner, and repo are required.");
        }

        List<Map<String, Object>> issues = gitHubService.getRepoIssues(token, owner, repo);

        return ResponseEntity.ok(issues);
    }


}
