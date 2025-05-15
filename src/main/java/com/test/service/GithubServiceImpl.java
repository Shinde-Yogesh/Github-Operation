package com.test.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GithubServiceImpl implements GithubService {

    private final WebClient webClient;

    public GithubServiceImpl() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    @Override
    public boolean authenticateWithToken(String token) {
        try {
            String user = webClient.get()
                    .uri("/user")
                    .headers(headers -> headers.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return user != null && !user.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
