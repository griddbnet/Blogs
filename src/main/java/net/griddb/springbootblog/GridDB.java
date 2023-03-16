
package net.griddb.springbootblog;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;

public class GridDB {

    public GridStore store ;
    Collection<String, Device> devCol ;

    public GridDB() {
        try {
		Properties props = new Properties();
		props.setProperty("notificationMember", "127.0.0.1:10001");
		props.setProperty("clusterName", "myCluster");
		props.setProperty("user", "admin"); 
		props.setProperty("password", "admin");
		store = GridStoreFactory.getInstance().getGridStore(props);
    	devCol = store.putCollection("sbDEVICES", Device.class);
        } catch (Exception e) {
            System.out.println("Could not get Gridstore instance, exitting.");
            System.exit(-1);
        }
    }

    public List<Device> getDevices() {
        List<Device> retval= new ArrayList<Device>();
        try {
		    Query<Device> query = devCol.query("select *");
            RowSet<Device> rs = query.fetch(false);
            while(rs.hasNext()) {
                Device dev = rs.next();
                retval.add(dev);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return retval;

    }
    public Device getDevice(String id) {
        try {
		    Query<Device> query = devCol.query("select * where id='"+id+"'");
            RowSet<Device> rs = query.fetch(false);
            if (rs.hasNext()) {
                Device dev = rs.next();
                return dev;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    public void putDevice(Device dev) {

        try {   
            System.out.println("dev="+dev);
            devCol.setAutoCommit(false);
            devCol.put(dev);
            devCol.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void deleteDevice(String id) {

        try {   
            System.out.println("deleting dev="+id);
            devCol.setAutoCommit(false);
            devCol.remove(id);
            devCol.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
