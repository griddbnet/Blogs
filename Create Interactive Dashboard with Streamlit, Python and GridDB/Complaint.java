import java.util.Date;
import com.toshiba.mwcloud.gs.Geometry;
import com.toshiba.mwcloud.gs.RowKey;
public class Complaint {
    @RowKey int CMPLNT_NUM;
    String CMPLNT_FR_DT;
    String CMPLNT_FR_TM;
    String CMPLNT_TO_DT;
    String CMPLNT_TO_TM;
    int ADDR_PCT_CD;
    String RPT_DT;
    int KY_CD;
    String OFNS_DESC;
    int PD_CD;
    String PD_DESC;
    String CRM_ATPT_CPTD_CD;
    String LAW_CAT_CD;
    String BORO_NM;
    String LOC_OF_OCCUR_DESC;
    String PREM_TYP_DESC;
    String JURIS_DESC;
    int JURISDICTION_CODE;
    String PARKS_NM;
    String HADEVELOPT;
    String HOUSING_PSA;
    int X_COORD_CD;
    int Y_COORD_CD;
    String SUSP_AGE_GROUP;
    String SUSP_RACE;
    String SUSP_SEX;
    int TRANSIT_DISTRICT;
    float Latitude;
    float Longitude;
    String Lat_Lon;
    String PATROL_BORO;
    String STATION_NAME;
    String VIC_AGE_GROUP;
    String VIC_RACE;
    String VIC_SEX;
    Date CMPLNT_FR;
    Date CMPLNT_TO;
    public String toString() {

        return this.CMPLNT_NUM+": " +this.CMPLNT_FR_DT+" @ "+this.Lat_Lon;
    }
}
