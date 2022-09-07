package net.griddb.tstaxiblog;
import java.io.IOException;
import java.util.Date;
import org.apache.parquet.hadoop.ParquetInputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

import java.util.Properties;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.TimeSeries;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;


public class IngestParquet {


    private static int writeGroup(TimeSeries ts, Group g) throws GSException {

        int fieldCount = g.getType().getFieldCount();
        int valueCount = g.getFieldRepetitionCount(0);
        int rowcount=0;
        for (int index = 0; index < valueCount; index++) {
            TaxiTrip r = new TaxiTrip();
            for (int field = 0; field < fieldCount; field++) {
            
                try {
                Type fieldType = g.getType().getType(field);
                String fieldName = fieldType.getName();

                if (fieldType.isPrimitive()) {
                    switch(fieldName) {
                        case "tpep_pickup_datetime":
                            r.tpep_pickup_datetime = new Date(g.getLong(field, index)/1000);
                            break;
                        case "tpep_dropoff_datetime":
                            r.tpep_dropoff_datetime = new Date(g.getLong(field, index)/1000);
                            break;
                        case "VendorID":
                            r.VendorID = g.getLong(field, index);
                            break;
                        case "passenger_count":
                            r.passenger_count = g.getDouble(field, index);
                            break;
                        case "trip_distance":
                            r.trip_distance = (float)g.getDouble(field, index);
                            break;
                        case "RatecodeID":
                            r.RatecodeID = g.getDouble(field, index);
                            break;
                        case "store_and_fwd_flag":
                            r.store_and_fwd_flag = g.getString(field, index);
                            break;
                        case "PULocationID":
                            r.PULocationID = g.getLong(field, index);
                            break;
                        case "DOLocationID":
                            r.DOLocationID = g.getLong(field, index);
                            break;
                        case "payment_type":
                            r.payment_type = g.getLong(field, index);
                            break;
                        case "fare_amount":
                            r.fare_amount = (float)g.getDouble(field, index);
                            break;
                        case "extra":
                            r.extra = g.getDouble(field, index);
                            break;
                        case "mta_tax":
                            r.mta_tax = g.getDouble(field, index);
                            break;
                        case "tip_amount":
                            r.tip_amount = g.getDouble(field, index);
                            break;
                        case "tolls_amount":
                            r.tolls_amount = g.getDouble(field, index);
                            break;
                        case "improvement_surcharge":
                            r.improvement_surcharge = g.getDouble(field, index);
                            break;
                        case "total_amount":
                            r.total_amount = g.getDouble(field, index);
                            break;
                        case "congestion_surcharge":
                            r.congestion_surcharge = g.getDouble(field, index);
                            break;
                        case "airport_fee":
                            r.airport_fee = g.getDouble(field, index);
                            break;
                        default:
                            System.out.println("Unknown field: "+fieldName+" value="+g.getValueToString(field, index));
                    }
                } 
                } catch (Exception e) {
                }
            }
            rowcount++;
            ts.put(r);
            //System.out.println("r="+r);
        }
        return rowcount;
    }

    public static void main(String[] args) throws IllegalArgumentException, GSException {

		Properties props = new Properties();
		props.setProperty("notificationMember", "127.0.0.1:10001");
		props.setProperty("clusterName", "myCluster");
		props.setProperty("user", "admin"); 
		props.setProperty("password", "admin");
		GridStore store = GridStoreFactory.getInstance().getGridStore(props);

        store.dropContainer("NYC_TaxiTrips");
		// Create a Collection (Delete if schema setting is NULL)
		TimeSeries<TaxiTrip> ts = store.putTimeSeries("NYC_TaxiTrips", TaxiTrip.class);


        Path path = new Path("/tmp/yellow_tripdata_2021-01.parquet");
        Configuration conf = new Configuration();

        int rowCount=0;
        try {
            ParquetMetadata readFooter = ParquetFileReader.readFooter(conf, path, ParquetMetadataConverter.NO_FILTER);
            MessageType schema = readFooter.getFileMetaData().getSchema();
            ParquetFileReader r = new ParquetFileReader(conf, path, readFooter);

            PageReadStore pages = null;
            try {
                while (null != (pages = r.readNextRowGroup())) {
                    final long rows = pages.getRowCount();
                    System.out.println("Number of rows: " + rows);

                    final MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
                    final RecordReader recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));
                    for (int i = 0; i < rows; i++) {
                        final Group g = (Group)recordReader.read();
                        rowCount += writeGroup(ts, g);

                    }
                }
            } finally {
                r.close();
            }
        } catch (IOException e) {
            System.out.println("Error reading parquet file.");
            e.printStackTrace();
        }

        if (rowCount == 0)
            System.out.println("Did not ingest any Taxi Trips, please check the path for your Parquet file");
        else {      
            System.out.println("Ingested "+rowCount+" taxi trips");
        }      
    }
}
