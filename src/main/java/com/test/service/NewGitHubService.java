package com.test.service;

import com.test.model.GitHubRepo;
import com.test.model.GitHubUser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NewGitHubService {

    // Create a logger instance
    private static final Logger logger = LoggerFactory.getLogger(NewGitHubService.class);

    //for the user Login using Token
    public GitHubUser authenticateWithToken(String token) {
        String url = "https://api.github.com/user";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubUser> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubUser.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            // log error if necessary
            logger.error("An error occurred: {}", e.getMessage(), e);
        }

        return null;
    }

    //for the all repositories

    public List<GitHubRepo> getAuthenticatedUserRepositories(String token) {
        String url = "https://api.github.com/user/repos";  // returns both public + private

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubRepo[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    GitHubRepo[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            // log error
            logger.error("An error occurred: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }


    //for the structure

//    public Map<String, Object> getRepositoryStructure(String token, String owner, String repo) {
//        try {
//            // Step 1: Get default branch
//            String repoUrl = "https://api.github.com/repos/" + owner + "/" + repo;
//            HttpHeaders headers = new HttpHeaders();
//            headers.setBearerAuth(token);
//            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//            HttpEntity<String> entity = new HttpEntity<>(headers);
//
//            RestTemplate restTemplate = new RestTemplate();
//
//            ResponseEntity<Map> repoResponse = restTemplate.exchange(
//                    repoUrl,
//                    HttpMethod.GET,
//                    entity,
//                    Map.class
//            );
//
//            if (!repoResponse.getStatusCode().is2xxSuccessful()) {
//                return null;
//            }
//
//            String defaultBranch = (String) repoResponse.getBody().get("default_branch");
//
//            // Step 2: Get tree structure
//            String treeUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/git/trees/" + defaultBranch + "?recursive=1";
//
//            ResponseEntity<Map> treeResponse = restTemplate.exchange(
//                    treeUrl,
//                    HttpMethod.GET,
//                    entity,
//                    Map.class
//            );
//
//            if (!treeResponse.getStatusCode().is2xxSuccessful()) {
//                return null;
//            }
//
//            return treeResponse.getBody();
//        } catch (Exception e) {
//            // log or handle exception
//            logger.error("An error occurred: {}", e.getMessage(), e);
//            return null;
//        }
//    }


    public List<String> getRepositoryFilePaths(String token, String owner, String repo) {
        try {
            // Step 1: Get default branch
            String repoUrl = "https://api.github.com/repos/" + owner + "/" + repo;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Map> repoResponse = restTemplate.exchange(
                    repoUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!repoResponse.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            String defaultBranch = (String) repoResponse.getBody().get("default_branch");

            // Step 2: Get tree structure
            String treeUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/git/trees/" + defaultBranch + "?recursive=1";

            ResponseEntity<Map> treeResponse = restTemplate.exchange(
                    treeUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!treeResponse.getStatusCode().is2xxSuccessful()) {
                return null;
            }

            // Extract only the file paths
            List<Map<String, Object>> tree = (List<Map<String, Object>>) treeResponse.getBody().get("tree");

            List<String> paths = new ArrayList<>();

            for (Map<String, Object> item : tree) {
                String path = (String) item.get("path");
                if (path != null) {
                    paths.add(path);
                }
            }

            return paths;
        } catch (Exception e) {
            return null;
        }
    }


}
