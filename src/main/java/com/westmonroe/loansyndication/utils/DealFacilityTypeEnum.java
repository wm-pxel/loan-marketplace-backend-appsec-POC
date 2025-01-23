package com.westmonroe.loansyndication.utils;

public enum DealFacilityTypeEnum {
    TERM(6, "Term", 1),
    REVOLVER(7, "Revolver", 2),
    REVOLVING_TERM_LOAN(8, "Revolving Term Loan",3),
    DELAYED_DRAW_TERM_LOAN(9, "Delayed Draw Term Loan", 4),
    OTHER(10, "Other", 5);
    private final String name;
    private final long id;
    private final long order;

    private DealFacilityTypeEnum(long id, String name, long order) {
        this.order = order;
        this.name = name;
        this.id = id;
    }

    public long getOrder() {
        return this.order;
    }

    public String getName() {
        return this.name;
    }

    public long getId() { return this.id; }
}
