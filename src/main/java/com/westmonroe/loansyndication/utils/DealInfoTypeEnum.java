package com.westmonroe.loansyndication.utils;

public enum DealInfoTypeEnum {

    DEAL_OVERVIEW(1, "Deal Overview"),
    BORROWER_INFO(2, "Borrower Information"),
    FINANCIAL_METRICS(3, "Financial Metrics"),
    FACILITY(4, "Facility");

    private final long id;
    private final String name;

    private DealInfoTypeEnum(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static DealInfoTypeEnum valueOfId(long id) {

        for ( DealInfoTypeEnum ate : values() ) {
            if ( ate.id == id ) {
                return ate;
            }
        }

        return null;
    }

    public static DealInfoTypeEnum valueOfName(String name) {

        for ( DealInfoTypeEnum ate : values() ) {
            if ( ate.name.equals(name) ) {
                return ate;
            }
        }

        return null;
    }

}