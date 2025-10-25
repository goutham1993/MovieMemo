package com.entertainment.moviememo.data.enums;

public enum LocationType {
    HOME("Home"),
    THEATER("Theater"),
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
