package com.westmonroe.loansyndication.utils;

public enum DealRelationEnum {

    ORIGINATOR("O", "Originator"),
    PARTICIPANT("P", "Participant"),
    UNKNOWN("U", "Unknown");

    private final String code;
    private final String description;

    private DealRelationEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

}