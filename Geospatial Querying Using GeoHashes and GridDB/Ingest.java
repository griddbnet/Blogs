import java.util.Arrays;
import java.util.Properties;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.math.RoundingMode;
import java.util.Date;
import java.io.Reader;
import java.io.FileReader;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.Geometry;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.apache.commons.csv.*;

// Operaton on Collection data
public class Ingest {

public static Complaint parseCsvRecord(CSVRecord r) throws Exception {

    Complaint c = new Complaint();
    DateFormat df  = DateFormat.getDateInstance();

    try {
        c.CMPLNT_NUM = Integer.parseInt(r.get("CMPLNT_NUM"));

        String dt[] = r.get("CMPLNT_FR_DT").split("/");
        String tm[] = r.get("CMPLNT_FR_TM").split(":");
        c.CMPLNT_FR = new Date(Integer.parseInt(dt[2])-1900, Integer.parseInt(dt[0])-1, Integer.parseInt(dt[1]), Integer.parseInt(tm[0]), Integer.parseInt(tm[1]), Integer.parseInt(tm[2]));
        //c.CMPLNT_TO_DT = df.parse(r.get("CMPLNT_TO_DT")+" "+r.get("CMPLNT_TO_TM"));
        c.ADDR_PCT_CD = Integer.parseInt(r.get("ADDR_PCT_CD"));
        //c.RPT_DT = df.parse(r.get("RPT_DT"));
        c.KY_CD = Integer.parseInt(r.get("KY_CD"));
        c.OFNS_DESC = r.get("OFNS_DESC");
        c.PD_CD = Integer.parseInt(r.get("PD_CD"));
        c.PD_DESC = r.get("PD_DESC");
        c.CRM_ATPT_CPTD_CD = r.get("CRM_ATPT_CPTD_CD");
        c.LAW_CAT_CD = r.get("LAW_CAT_CD");
        c.BORO_NM = r.get("BORO_NM");
        c.LOC_OF_OCCUR_DESC = r.get("LOC_OF_OCCUR_DESC");
        
        // skip some values
        c.Latitude = Float.parseFloat(r.get("Latitude"));
        c.Longitude = Float.parseFloat(r.get("Longitude"));
        c.Lat_Lon = r.get("Lat_Lon");
        c.GEOHASH=  GeohashUtils.encodeLatLon(c.Latitude, c.Longitude);
        //c.Lat_Lon =   Geometry.valueOf("POINT("+c.Longitude+" "+c.Latitude+")");
    } catch(Exception e) {
        e.printStackTrace();
    }

    return c; 

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
        Reader in = new FileReader("rows.csv");
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        int i=0;

        for (CSVRecord record : records) {
            try {
                if (i++ % 1000 == 0) System.out.println("Ingesting "+ i);
                Complaint c = parseCsvRecord(record);
                if(c != null) {
                    String cName = "geo_"+GeohashUtils.encodeLatLon(c.Latitude, c.Longitude, 6);
                    store.dropContainer(cName);
                    System.out.println("adding "+c+" to "+cName); 
		            Collection<String, Complaint> col = store.putCollection(cName, Complaint.class);
                    col.setAutoCommit(false);
                    col.put(c);
                    col.commit();
                }
            } catch(Exception e) {
                System.out.println("Failed to ingest "+i);
                System.out.println(e);
            }
        }

		store.close();
	}

}
