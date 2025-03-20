package mycode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

import com.toshiba.mwcloud.gs.RowKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VcsActivityDTO {
  @RowKey
  public Date timestamp; // Time of the activity
  private String eventType; // Event type: commit, pull request, merge, branch
  private String developerId; // Developer who performed the activity
  private String repositoryId; // Repository ID or name
  private String branch; // Branch associated with the activity
  private String status; // Status (e.g., success, open, merged, conflict)
}
