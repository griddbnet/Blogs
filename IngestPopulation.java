import java.util.Arrays;
import java.util.Properties;
import java.util.HashMap;
import java.text.DateFormat;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.Reader;
import java.io.FileReader;
import com.toshiba.mwcloud.gs.TimeSeries;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.Geometry;
import org.apache.commons.csv.*;

public class IngestPopulation {

    static class Population {
        @RowKey Date date;
        int value;
    }


public static Population parseCsvRecord(CSVRecord r) throws Exception {

    Population pop = new Population();

    try {
        String dateString = r.get("date");
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date tm1 = sf.parse(dateString);
    
        pop.value = Integer.parseInt(r.get("value"));
        pop.date = tm1;
    } catch(Exception e) {
        return null;
    }

    return pop; 

}

public static void main(String[] args) throws Exception {

		// Get a GridStore instance
		Properties props = new Properties();
		props.setProperty("notificationAddress", "239.0.0.1");
		props.setProperty("notificationPort", "31999");
		props.setProperty("clusterName", "defaultCluster");
		props.setProperty("user", "admin");
		props.setProperty("password", "admin");
		GridStore store = GridStoreFactory.getInstance().getGridStore(props);
 
        Reader in = new FileReader("population.csv");
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        int i=0;
        for (CSVRecord record : records) {
            try {
                Population pop = parseCsvRecord(record);
                if(pop != null) {
		            TimeSeries<Population> ts = store.putTimeSeries("population", Population.class);
                    ts.setAutoCommit(false);
                    ts.put(pop);
                    ts.commit();
                }
            } catch(Exception e) {
                System.out.println("Failed to ingest "+i);
                System.out.println(e);
            }
        }

		store.close();
	}

}
