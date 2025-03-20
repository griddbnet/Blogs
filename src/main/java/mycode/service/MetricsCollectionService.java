package mycode.service;

import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.toshiba.mwcloud.gs.*;
import mycode.dto.VcsActivityDTO;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Service
public class MetricsCollectionService {
  @Autowired
  GridStore store;

  @Autowired
  RestTemplate restTemplate;

  @Value("${github.api.token}")
  private String githubApiToken;

  @Value("${github.api.base-url}")
  private String githubBaseUrl;

  @Scheduled(fixedRate = 2222260) // Collect metrics every minute
  public void collectMetrics() throws GSException, JsonMappingException, JsonProcessingException, ParseException {
    String repoOwner = "microsoft"; 
    String repoName = "vscode"; 

    List<VcsActivityDTO> commits = getCommits(repoOwner, repoName);
   List<VcsActivityDTO> pullRequests = getPullRequests(repoOwner, repoName);
    List<VcsActivityDTO> branchEvents = getBranchEvents(repoOwner, repoName);

   System.out.println("Collected Commits: " + commits.size());
   System.out.println("Collected Pull Requests: " + pullRequests.size());
    System.out.println("Collected Pull Requests: " + branchEvents.size());
    List<VcsActivityDTO> result = new ArrayList<>();
    result.addAll(commits);
    result.addAll(pullRequests);
    result.addAll(branchEvents);
    TimeSeries<VcsActivityDTO> ts = store.putTimeSeries("vcsData", VcsActivityDTO.class);
    for (VcsActivityDTO activity : result) {
       ts.append(activity);
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + githubApiToken);
    return headers;
  }

  // Method to retrieve commits from a specific repository
  public List<VcsActivityDTO> getCommits(String repoOwner, String repoName) throws ParseException {
    String url = UriComponentsBuilder.fromHttpUrl(githubBaseUrl)
        .path("/repos/{owner}/{repo}/commits")
        .buildAndExpand(repoOwner, repoName)
        .toUriString();

    HttpHeaders headers = createHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
    List<VcsActivityDTO> commitActivities = new ArrayList<>();

    List<Map<String, Object>> commits = (List<Map<String, Object>>) response.getBody();
    if (commits != null) {
      for (Map<String, Object> commit : commits) {
        commitActivities.add(mapCommitToVcsActivityDTO(commit));
      }
    }

    return commitActivities;
  }

  public List<VcsActivityDTO> getPullRequests(String repoOwner, String repoName) throws ParseException {
    String url = UriComponentsBuilder.fromHttpUrl(githubBaseUrl)
        .path("/repos/{owner}/{repo}/pulls")
        .queryParam("state", "all") // Retrieves both open and closed pull requests
        .buildAndExpand(repoOwner, repoName)
        .toUriString();

    HttpHeaders headers = createHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
    List<VcsActivityDTO> prActivities = new ArrayList<>();

    List<Map<String, Object>> pullRequests = (List<Map<String, Object>>) response.getBody();
    if (pullRequests != null) {
      for (Map<String, Object> pr : pullRequests) {
        prActivities.add(mapPullRequestToVcsActivityDTO(pr));
      }
    }

    return prActivities;
  }

  private VcsActivityDTO mapCommitToVcsActivityDTO(Map<String, Object> commitData) throws ParseException {
    Map<String, Object> commitInfo = (Map<String, Object>) commitData.get("commit");
    Map<String, Object> authorInfo = (Map<String, Object>) commitInfo.get("author");

    String eventType = "Commit";
    String developerId = (String) authorInfo.get("name");
    String repositoryId = (String) commitInfo.get("url"); 
    String branch = "main"; 
    String status = "Success";
    String timestamp = (String) authorInfo.get("date");

    return new VcsActivityDTO(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(timestamp), eventType, developerId, repositoryId, branch, status);
  }

  private VcsActivityDTO mapPullRequestToVcsActivityDTO(Map<String, Object> prData) throws ParseException {
    String eventType = "Pull Request";
    Map<String, Object> userInfo = (Map<String, Object>) prData.get("user");
    String developerId = (String) userInfo.get("login");
    String repositoryId = (String) userInfo.get("repos_url");; 
    String branch = (String) ((Map<String, Object>)prData.get("base")).get("ref"); 
    String status = (String) prData.get("state"); 
    String timestamp = (String) prData.get("created_at");
    return new VcsActivityDTO(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(timestamp), eventType, developerId, repositoryId, branch, status);
  }

  public List<VcsActivityDTO> getBranchEvents(String repoOwner, String repoName) throws ParseException{
    String url = UriComponentsBuilder.fromHttpUrl(githubBaseUrl)
        .path("/repos/{owner}/{repo}/events")
        .buildAndExpand(repoOwner, repoName)
        .toUriString();

    HttpHeaders headers = createHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
    List<VcsActivityDTO> branchActivities = new ArrayList<>();

    List<Map<String, Object>> events = response.getBody();
    if (events != null) {
      for (Map<String, Object> eventData : events) {
        String eventType = (String) eventData.get("type");
        if ("CreateEvent".equals(eventType) || "DeleteEvent".equals(eventType)) {
          Map<String, Object> payload = (Map<String, Object>) eventData.get("payload");
          String refType = (String) payload.get("ref_type");
          if ("branch".equals(refType)) {
            String branchName = (String) payload.get("ref");
            String developerId = (String) ((Map<String, Object>)eventData.get("actor")).get("login");  
            String timestamp = (String) eventData.get("created_at");

            // Map to VcsActivityDTO
            VcsActivityDTO activity = new VcsActivityDTO(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(timestamp),
                eventType.equals("CreateEvent") ? "Branch Creation" : "Branch Deletion",
                developerId,
                repoName,
                branchName,
                "Success"
                );
            branchActivities.add(activity);
          }
        }
      }
    }
    return branchActivities;
  }

}
