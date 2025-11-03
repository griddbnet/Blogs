package net.griddb;

import java.util.Date;
import com.toshiba.mwcloud.gs.RowKey;

public class Device {
    @RowKey
    Date ts;
    double co;
    double humidity;
    boolean light;
    double lpg;
    boolean motion;
    double smoke;
    double temp;

    @Override
    public String toString() {
        return "Device{" +
                "ts=" + ts +
                ", co=" + co +
                ", humidity=" + humidity +
                ", light=" + light +
                ", lpg=" + lpg +
                ", motion=" + motion +
                ", smoke=" + smoke +
                ", temp=" + temp +
                '}';
    }

}
