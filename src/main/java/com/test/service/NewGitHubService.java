package com.test.service;

import com.test.model.GitHubRepo;
import com.test.model.GitHubUser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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


}
