package com.cs180.ucrtinder.ucrtinder.tindercard;

/**
 * Created by nirav on 05/10/15.
 */
public class Data {

    private String description;
    private String imagePath;
    private String UserString;

    public Data(String imagePath, String description, String userString) {
        this.imagePath = imagePath;
        this.description = description;
        this.UserString = userString;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getUserString() {
        return UserString;
    }


}
