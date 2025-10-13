package com.entertainment.moviememo.data.enums;

public enum LocationType {
    THEATER("Theater"),
    HOME("Home"),
    FRIENDS_HOME("Friend's Home"),
    OTHER("Other");
    
    private final String displayName;
    
    LocationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
