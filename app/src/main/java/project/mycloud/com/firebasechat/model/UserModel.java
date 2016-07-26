package project.mycloud.com.firebasechat.model;

/**
 * Created by admin on 2016-07-25.
 */
public class UserModel {
    private String id;
    private String name;
    private String photo_profile;

    //
    public UserModel(){}

    //
    public UserModel(String name,String photo_profile, String id) {
        this.name = name;
        this.photo_profile = photo_profile;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_profile() {
        return photo_profile;
    }

    public void setPhoto_profile(String photo_profile) {
        this.photo_profile = photo_profile;
    }
}
