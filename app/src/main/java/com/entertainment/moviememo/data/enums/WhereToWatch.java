package com.entertainment.moviememo.data.enums;

public enum WhereToWatch {
    THEATER("Theater"),
    OTT_STREAMING("OTT/Streaming"),
    OTHER("Other");
    
    private final String displayName;
    
    WhereToWatch(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

