package com.test.service;

import com.test.model.GitHubRepo;
import com.test.model.GitHubUser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

/*
the code work for fail null pointer exception

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

 */

    public List<String> getRepositoryFilePaths(String token, String owner, String repo) {
        List<String> paths = new ArrayList<>();
        try {
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
                System.err.println("Repo metadata fetch failed: " + repoResponse.getStatusCode());
                return paths;  // return empty list instead of null
            }

            String defaultBranch = (String) repoResponse.getBody().get("default_branch");
            if (defaultBranch == null) {
                System.err.println("No default branch found.");
                return paths;
            }

            String treeUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/git/trees/" + defaultBranch + "?recursive=1";

            ResponseEntity<Map> treeResponse = restTemplate.exchange(
                    treeUrl,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!treeResponse.getStatusCode().is2xxSuccessful()) {
                System.err.println("Tree structure fetch failed: " + treeResponse.getStatusCode());
                return paths;
            }

            List<Map<String, Object>> tree = (List<Map<String, Object>>) treeResponse.getBody().get("tree");
            if (tree != null) {
                for (Map<String, Object> item : tree) {
                    String path = (String) item.get("path");
                    if (path != null) {
                        paths.add(path);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Exception while fetching repo paths: " + e.getMessage());
            e.printStackTrace();
        }

        return paths;  // always return a list, never null
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

    // for the push file on github

    /*
    public Optional<String> findMatchingGitHubFolder(String localFilePath, List<String> repoPaths) {
        Path localPath = Paths.get(localFilePath).normalize();
        List<String> segments = new ArrayList<>();

        for (Path part : localPath) {
            segments.add(part.toString().toLowerCase());
        }

        List<String> candidates = List.of("controller", "service", "repository", "model");

        for (String segment : segments) {
            if (candidates.contains(segment)) {
                for (String repoPath : repoPaths) {
                    if (repoPath.toLowerCase().contains("/" + segment)) {
                        int lastSlash = repoPath.lastIndexOf('/');
                        if (lastSlash != -1) {
                            return Optional.of(repoPath.substring(0, lastSlash));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    public Map<String, Object> pushSmartToMatchingFolder(
            String token,
            String owner,
            String repo,
            String localFilePath,
            String message) {

        // Step 1: Get all repo file paths
        List<String> repoPaths = getRepositoryFilePaths(token, owner, repo);

        // Step 2: Extract filename from local path
        String fileName = Paths.get(localFilePath).getFileName().toString();

        // Step 3: Match GitHub folder
        Optional<String> matchedGitHubFolder = findMatchingGitHubFolder(localFilePath, repoPaths);

        if (matchedGitHubFolder.isEmpty()) {
            return Map.of("error", "Could not find matching folder in GitHub repo");
        }

        // Step 4: Compose full GitHub path
        String githubPath = matchedGitHubFolder.get() + "/" + fileName;

        // Step 5: Read local file content
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(localFilePath));
            String rawContent = new String(fileBytes, StandardCharsets.UTF_8);

            return pushCodeSmart(token, owner, repo, githubPath, message, rawContent);

        } catch (IOException e) {
            return Map.of("error", "Failed to read file: " + e.getMessage());
        }
    }


    public Map<String, Object> pushCodeSmart(
            String token, String owner, String repo, String path, String message, String rawContent) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path;

            // Step 1: Try to fetch SHA if file exists
            String sha = null;
            try {
                HttpHeaders getHeaders = new HttpHeaders();
                getHeaders.setBearerAuth(token);
                getHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders);

                ResponseEntity<Map> getResponse = restTemplate.exchange(
                        url, HttpMethod.GET, getEntity, Map.class);

                if (getResponse.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> body = getResponse.getBody();
                    if (body != null && body.containsKey("sha")) {
                        sha = body.get("sha").toString();
                    }
                }
            } catch (HttpClientErrorException.NotFound ignored) {
                // File does not exist — that's OK, we are creating
            }

            // Step 2: Prepare Base64 content and commit payload
            String base64Content = Base64.getEncoder().encodeToString(
                    rawContent.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("content", base64Content);
            if (sha != null) requestBody.put("sha", sha);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> putEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> putResponse = restTemplate.exchange(
                    url, HttpMethod.PUT, putEntity, Map.class);

            return putResponse.getBody();
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
*/

    public void pushFileToGitHub(String token, String owner, String repo, String localFilePath, String commitMessage) {
        try {
            // 1. Read local file content and encode to Base64
            byte[] fileBytes = Files.readAllBytes(Paths.get(localFilePath));
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);

            // 2. Convert local path to GitHub-relative path
            String baseProjectPath = "D:/Spring And SpringBoot Projects/ResponseEntity_Example/"; // Adjust as needed
            String githubPath = localFilePath.replace(baseProjectPath, "").replace("\\", "/");

            // 3. Get the SHA of existing file if any
            String getFileUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + githubPath;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            HttpEntity<String> getEntity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();

            String sha = null;
            try {
                ResponseEntity<Map> getResponse = restTemplate.exchange(
                        getFileUrl,
                        HttpMethod.GET,
                        getEntity,
                        Map.class
                );
                if (getResponse.getStatusCode().is2xxSuccessful() && getResponse.getBody() != null) {
                    sha = (String) getResponse.getBody().get("sha");
                }
            } catch (HttpClientErrorException.NotFound ex) {
                // File does not exist, will be created
            }

            // 4. Prepare request body for commit
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", commitMessage);
            requestBody.put("content", base64Content);
            requestBody.put("branch", "main"); // or whatever branch you are targeting
            if (sha != null) {
                requestBody.put("sha", sha); // needed for updating existing file
            }

            HttpEntity<Map<String, Object>> putEntity = new HttpEntity<>(requestBody, headers);

            // 5. Push the file (create or update)
            ResponseEntity<String> pushResponse = restTemplate.exchange(
                    getFileUrl,
                    HttpMethod.PUT,
                    putEntity,
                    String.class
            );

            if (pushResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ File pushed to GitHub at: " + githubPath);
            } else {
                System.err.println("❌ Failed to push file: " + pushResponse.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("🚨 Exception during file push: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
