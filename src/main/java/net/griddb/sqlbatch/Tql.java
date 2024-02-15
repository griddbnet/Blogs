package net.griddb.sqlbatch;
import com.toshiba.mwcloud.gs.*;

import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

public class Tql {

    public static GridStore store;

    public Tql ()  throws GSException{
        
        try {
            Properties props = new Properties();
            props.setProperty("notificationMember", "127.0.0.1:10001");
            props.setProperty("clusterName", "myCluster");
            props.setProperty("user", "admin"); 
            props.setProperty("password", "admin");
            store = GridStoreFactory.getInstance().getGridStore(props);
            
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }

    protected  void runSinglePut(SerialBlob blob) throws GSException, SerialException, SQLException {
		Collection<String, Row> col = store.getCollection("SampleTQL_BlobData");	
		col.setAutoCommit(true);
 
		for (int i = 1; i <= 10000; i++) {
			Row row;
			row = col.createRow();
			row.setInteger(0, i);
			row.setBlob(1, blob);
			col.put(row);
		}
	}

	protected  void runMulti (SerialBlob blob) throws GSException, SerialException, SQLException {
		final Map<String, List<Row>> rowListMap = new HashMap<String, List<Row>>();
		Collection<String, Row> col = store.getCollection("MultiPutTQL_Blobdata");	
		col.setAutoCommit(true);

		List<Row> rows = new ArrayList<>();
		for (int i = 1; i <= 10000; i++) {
			Row row;
			row = col.createRow();
			row.setInteger(0, i);
			row.setBlob(1, blob);
			rows.add(row);
		}
		rowListMap.put("MultiPutTQL_Blobdata", rows);
		store.multiPut(rowListMap);
	}
}

