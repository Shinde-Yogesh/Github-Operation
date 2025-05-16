package com.test.model;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubUser {

    private String login;
    private Long id;
    private String name;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String email;

    // Add more fields as needed

    // Getters and Setters
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
