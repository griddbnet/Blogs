package net.griddb;

import com.toshiba.mwcloud.gs.*;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) {
        GridDBJdbc griddbSql = new GridDBJdbc();
        try {
            griddbSql.CreateTable("exampleJdbc");
            griddbSql.DumpContainer("exampleJdbc");
            griddbSql.queryTimeBucketedAverages_SQL("device1");
        } catch (SQLException e) {
            System.out.println(e);
        }

        System.out.println("Testing GridDB NoSQL");

        try {
            GridDB griddb = new GridDB();
            griddb.CreateContainer("deviceExample");
            griddb.WriteToContainer("deviceExample");
            griddb.DumpContainer("exampleJdbc");
            griddb.MultiPut();
            griddb.MultiGet();

            griddb.queryAverageTemperature("device1");
            griddb.queryMaxHumidity("device1");
            griddb.queryMotionCount("device1");
            griddb.queryTimeBoundAverage("device1");
            griddb.queryTimeBucketedAverages("device1");

        } catch (GSException gse) {
            gse.printStackTrace();
        }

    }
}
