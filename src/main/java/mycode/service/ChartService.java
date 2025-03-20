package mycode.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;

import mycode.dto.VcsActivityDTO;

@Service
public class ChartService {

  @Autowired
  GridStore store;

  public List<VcsActivityDTO> getVcsEvents() throws Exception {

    Container<?, Row> container = store.getContainer("vcsData");
    if (container == null) {
      throw new Exception("Container not found.");
    }
    List<VcsActivityDTO> eventList = new ArrayList<>();


    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    Date now = new Date();

    String nowString = dateFormat.format(now);
    String startTime = "1971-12-23T18:18:52.000Z";

    String queryString = "select * where Timestamp >= TIMESTAMP('" + startTime
        + "') and Timestamp <= TIMESTAMP('" + nowString + "')";
    Query<Row> query = container.query(queryString);
    RowSet<Row> rs = query.fetch();

    while (rs.hasNext()) {
      Row row = rs.next();
      VcsActivityDTO event = new VcsActivityDTO();
      event.setTimestamp(row.getTimestamp(0));
      event.setEventType(row.getString(2));
      event.setDeveloperId(row.getString(3));
      event.setRepositoryId(row.getString(4));
      event.setBranch(row.getString(5));
      event.setStatus(row.getString(1));
      eventList.add(event);

    }
    return eventList;
  }

}
