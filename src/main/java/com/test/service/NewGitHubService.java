package com.test.service;

import com.test.model.GitHubRepo;
import com.test.model.GitHubUser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Base64;


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

    //for getting the file content

    public String getFileContent(String token, String owner, String repo, String path) {
        try {
            String contentUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(contentUrl, HttpMethod.GET, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) return null;

            String encodedContent = (String) response.getBody().get("content");
            String encoding = (String) response.getBody().get("encoding");

            if ("base64".equalsIgnoreCase(encoding) && encodedContent != null) {
                // GitHub may include newline characters, remove them
                encodedContent = encodedContent.replaceAll("\\s", "");
                byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
                return new String(decodedBytes);
            }

            return null;
        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage(), e);
            return null;
        }
    }

    //for getting the actual return type of data
    public byte[] getFileAsBytes(String token, String owner, String repo, String path) {
        try {
            String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (!response.getStatusCode().is2xxSuccessful()) return null;

            //checking the response
            System.out.println("Here is the Response :- ");
            System.out.println(response);

            String encodedContent = (String) response.getBody().get("content");
            String encoding = (String) response.getBody().get("encoding");

            if ("base64".equalsIgnoreCase(encoding)) {
                encodedContent = encodedContent.replaceAll("\\s", "");
                return Base64.getDecoder().decode(encodedContent);
            }

            return null;
        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage(), e);
            return null;
        }
    }

    public String detectContentType(String path) {
        if (path.endsWith(".java")) return "text/x-java-source";
        if (path.endsWith(".properties")) return "text/plain";
        if (path.endsWith(".txt")) return "text/plain";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".xml")) return "application/xml";
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream"; // default
    }

    //for getting the list of issue
    public List<Map<String, Object>> getRepoIssues(String token, String owner, String repo) {
        try {
            String url = "https://api.github.com/repos/" + owner + "/" + repo + "/issues";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    List.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) return Collections.emptyList();

            return response.getBody();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }


    //for the signle github issue

    public Map<String, Object> getSingleIssue(String token, String owner, String repo, String issueNumber) {
        try {
            String url = "https://api.github.com/repos/" + owner + "/" + repo + "/issues/" + issueNumber;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) return null;

            Map<String, Object> issue = response.getBody();

            // Remove if it's a pull request (optional)
            if (issue.containsKey("pull_request")) return null;

            // Enrich with comments
            String commentsUrl = url + "/comments";
            ResponseEntity<List> commentResponse = restTemplate.exchange(commentsUrl, HttpMethod.GET, entity, List.class);
            issue.put("comments", commentResponse.getStatusCode().is2xxSuccessful() ? commentResponse.getBody() : Collections.emptyList());

            return issue;

        } catch (Exception e) {
            return null;
        }
    }


}
