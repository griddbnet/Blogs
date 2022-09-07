package net.griddb.tstaxiblog;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Properties;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.TimeSeries;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.AggregationResult;
import org.apache.commons.lang3.time.DateUtils;


public class TaxiQuery {

    public static Date getDate (TimeSeries <TaxiTrip> ts, String sortmeth) throws GSException {
		Query<TaxiTrip> query = ts.query("select * order by tpep_pickup_datetime "+sortmeth +" limit 1");

		// Fetch and update the searched Row
		RowSet<TaxiTrip> rs = query.fetch(false);
		while (rs.hasNext()) {
			// Update the searched Row
			TaxiTrip t = rs.next();
            return t.tpep_pickup_datetime;
        }	

        return null;

    }
    public static void windowAggregate(GridStore store, String aggregation, Date start, Date end, int windowsize) throws GSException {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyy");  
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));  
        Calendar c = Calendar.getInstance();
        ArrayList<Query<AggregationResult>> queryList = new ArrayList<Query<AggregationResult>>();
        ArrayList<Date> dates = new ArrayList<Date>();
		Container<?, Row> ts = store.getContainer("NYC_TaxiTrips");
        start = DateUtils.truncate(start, windowsize);
        c.setTime(start);
        Date interval = c.getTime();


        do {
            c.add(windowsize, 1);
            String windowquery = "select "+aggregation+" where tpep_pickup_datetime > TO_TIMESTAMP_MS("+interval.getTime()+") and tpep_pickup_datetime < TO_TIMESTAMP_MS("+c.getTime().getTime()+")"; 
            Query<AggregationResult> q = ts.query(windowquery, AggregationResult.class);
            dates.add(interval);
            queryList.add(q);
            interval = c.getTime();
        } while (interval.getTime() <= end.getTime());

        store.fetchAll(queryList);

        for (int i = 0; i < queryList.size(); i++) {
            Query<AggregationResult> query = queryList.get(i);
            RowSet<AggregationResult> rs = query.getRowSet();
            while (rs.hasNext()) {
                AggregationResult result = rs.next();
                double value = result.getDouble();
                if (value != 0)
                    System.out.println("|"+sdf.format(dates.get(i))+" | "+ value+"|");
            }
        }


    }
    public static void timeSampling(GridStore store, String column, Date start, Date end, String windowstr) throws GSException {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyy");  
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));  
        Calendar c = Calendar.getInstance();
 
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00'Z'"); 
        df.setTimeZone(tz);
		Container<?, Row> ts = store.getContainer("NYC_TaxiTrips");

        String windowquery = "select time_sampling("+column+", TIMESTAMP('"+df.format(start)+"'), TIMESTAMP('"+df.format(end) +"') , 1, "+windowstr+") "; 
        System.out.println("windowquery="+windowquery);
        Query<Row> q = ts.query(windowquery);
        RowSet<Row> rs = q.fetch();
        while (rs.hasNext()) {
            Row result = rs.next();
            System.out.println("|"+ sdf.format(result.getTimestamp(0))+" | "+result.getDouble(10) +"|");
        }

    }

    public static void time_avg(GridStore store) throws GSException {

        Container <?, Row> ts = store.getContainer("NYC_TaxiTrips");
        Query<AggregationResult> query = ts.query("select time_avg(fare_amount)", AggregationResult.class);

        RowSet<AggregationResult> rs = query.fetch(false);
        if(rs.hasNext()) {
            AggregationResult result = rs.next();
            double value = result.getDouble();
            System.out.println("time_avg result="+value);
        }
    } 

    public static void main(String args[]) throws GSException{

		Properties props = new Properties();
		props.setProperty("notificationMember", "127.0.0.1:10001");
		props.setProperty("clusterName", "myCluster");
		props.setProperty("user", "admin"); 
		props.setProperty("password", "admin");
		GridStore store = GridStoreFactory.getInstance().getGridStore(props);

		TimeSeries<TaxiTrip> ts = store.putTimeSeries("NYC_TaxiTrips", TaxiTrip.class);

        Date start=new Date(1609459200000L);
        Date end=new Date((long)1640995200000L);

        System.out.println("# TIME SAMPLING");
        System.out.println("|---|---|");
        timeSampling(store, "fare_amount", start, end, "DAY");

        System.out.println("# Time Window Count");
        System.out.println("|---|---|");
        windowAggregate(store, "count(*)", start, end, Calendar.DATE);
     
        System.out.println("# Time Window Average");
        System.out.println("|---|---|");
        windowAggregate(store, "avg(fare_amount)", start, end, Calendar.DATE);
        
    }


}
