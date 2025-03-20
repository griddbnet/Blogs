package mycode.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import mycode.service.ChartService;
import mycode.dto.VcsActivityDTO;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChartController {

  @Autowired
  ChartService chartService;

  @Autowired
  private ObjectMapper objectMapper;

  @GetMapping("/charts")
  public String showCharts(Model model) {
    try {
      List<VcsActivityDTO> events = chartService.getVcsEvents();
      model.addAttribute("events", events);

      // Prepare data for charts
      Map<String, Integer> commitData = prepareCommitData(events);
      Map<String, Integer> prData = preparePullRequestData(events);

      // Convert Maps to JSON Strings for use in JavaScript in the Thymeleaf template
      String commitDataJson = objectMapper.writeValueAsString(commitData);
      String prDataJson = objectMapper.writeValueAsString(prData);

      model.addAttribute("commitDataJson", commitDataJson);
      model.addAttribute("prDataJson", prDataJson);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return "charts";
  }

  private Map<String, Integer> prepareCommitData(List<VcsActivityDTO> events) {
    Map<String, Integer> commitMap = new HashMap<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 

    for (VcsActivityDTO event : events) {
      if ("Commit".equals(event.getEventType())) {
        String timestamp = dateFormat.format(event.getTimestamp());
        commitMap.put(timestamp, commitMap.getOrDefault(timestamp, 0) + 1);
      }
    }
    return commitMap;
  }

  private Map<String, Integer> preparePullRequestData(List<VcsActivityDTO> events) {
    Map<String, Integer> prMap = new HashMap<>();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Date format

    for (VcsActivityDTO event : events) {
      if ("Pull Request".equals(event.getEventType())) {
        String timestamp = dateFormat.format(event.getTimestamp()); 
        prMap.put(timestamp, prMap.getOrDefault(timestamp, 0) + 1);
      }
    }
    return prMap;
  }
}
