package net.griddb.tstaxiblog;
import java.io.IOException;
import java.util.Date;
import com.toshiba.mwcloud.gs.RowKey;
class TaxiTrip {

        @RowKey Date tpep_pickup_datetime ;
        Date tpep_dropoff_datetime;
        long VendorID ;
        double passenger_count;
        double trip_distance ;
        double RatecodeID ;
        String store_and_fwd_flag ;
        long PULocationID ;
        long DOLocationID ;
        long payment_type ;
        double fare_amount;
        double extra;
        double mta_tax; 
        double tip_amount; 
        double tolls_amount;
        double improvement_surcharge;
        double total_amount;
        double congestion_surcharge;
        double airport_fee;
     
        public String toString() {
            return this.tpep_pickup_datetime+", "+this.tpep_dropoff_datetime+", "+this.VendorID+", "+this.passenger_count+", "+this.trip_distance+", "+this.RatecodeID+", "+this.store_and_fwd_flag+", "+this.PULocationID+", "+this.DOLocationID+", "+this.payment_type+", "+this.fare_amount+", "+this.extra+", "+this.mta_tax+", "+this.tip_amount+", "+this.tolls_amount+", "+this.improvement_surcharge+", "+this.total_amount+", "+this.congestion_surcharge+", "+this.airport_fee;
        }
    }


