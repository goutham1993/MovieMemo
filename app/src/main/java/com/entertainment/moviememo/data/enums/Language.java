package com.entertainment.moviememo.data.enums;

public enum Language {
    ENGLISH("English", "en"),
    TELUGU("తెలుగు", "te"),
    HINDI("हिन्दी", "hi"),
    TAMIL("தமிழ்", "ta"),
    KANNADA("ಕನ್ನಡ", "kn"),
    MALAYALAM("മലയാളം", "ml"),
    BENGALI("বাংলা", "bn"),
    MARATHI("मराठी", "mr"),
    GUJARATI("ગુજરાતી", "gu"),
    PUNJABI("ਪੰਜਾਬੀ", "pa"),
    OTHER("Other", "other");

    private final String displayName;
    private final String code;

    Language(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    public static Language fromCode(String code) {
        for (Language language : Language.values()) {
            if (language.code.equals(code)) {
                return language;
            }
        }
        return ENGLISH; // Default fallback
    }
}
