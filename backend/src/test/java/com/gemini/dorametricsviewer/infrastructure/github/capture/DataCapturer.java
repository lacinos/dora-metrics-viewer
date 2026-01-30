package com.gemini.dorametricsviewer.infrastructure.github.capture;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataCapturer {

    private static final String BASE_URL = "https://api.github.com";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || token.isEmpty()) {
            System.err.println("WARNING: GITHUB_TOKEN environment variable is not set. Requests may be rate-limited.");
        }

        String owner = "microsoft";
        String repo = "vscode";
        // Output directory relative to the project root
        String outputDir = "backend/src/test/resources/datasets/" + repo; 
        
        if (!Files.exists(Paths.get("backend")) && Files.exists(Paths.get("src"))) {
             outputDir = "src/test/resources/datasets/" + repo;
        }

        try {
            Files.createDirectories(Paths.get(outputDir));
            
            System.out.println("Capturing data for " + owner + "/" + repo + " into " + outputDir);

            // Fetch Releases (Deployments)
            // Fetching more to ensure we have enough data overlapping with PRs
            String releasesUrl = String.format("%s/repos/%s/%s/releases?per_page=50", BASE_URL, owner, repo);
            fetchAndSave(releasesUrl, token, Paths.get(outputDir, "deployments.json"));

            // Fetch Pull Requests (Changes) - state=closed to get merged ones
            // Fetching page 20 to get older PRs that are likely deployed in the releases we fetched
            // VS Code moves fast, so recent 50 PRs might be undeployed.
            String pullsUrl = String.format("%s/repos/%s/%s/pulls?state=closed&per_page=50&page=20", BASE_URL, owner, repo);
            fetchAndSave(pullsUrl, token, Paths.get(outputDir, "changes.json"));
            
            System.out.println("Data capture complete.");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void fetchAndSave(String url, String token, Path outputPath) throws IOException, InterruptedException {
        System.out.println("Fetching: " + url);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json");

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch data. Status: " + response.statusCode() + " Body: " + response.body());
        }

        Files.writeString(outputPath, response.body());
        System.out.println("Saved to: " + outputPath.toAbsolutePath());
    }
}