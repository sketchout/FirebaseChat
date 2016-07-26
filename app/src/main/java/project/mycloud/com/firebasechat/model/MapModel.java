package project.mycloud.com.firebasechat.model;

/**
 * Created by admin on 2016-07-25.
 */
public class MapModel {
    private String latitude;
    private String longitude;

    public MapModel(){}

    public MapModel(String latitude, String longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
