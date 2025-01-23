package com.westmonroe.loansyndication.utils;

public enum DealStageEnum {

    STAGE_1(1), STAGE_2(2), STAGE_3(3), STAGE_4(4), STAGE_5(5), STAGE_6(6)
    , STAGE_7(7), STAGE_8(8), STAGE_9(9);

    private int order;

    DealStageEnum(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public String getName() {
        switch(this) {
            case STAGE_1 -> { return "Event Created"; }
            case STAGE_2 -> { return "Gathering Interest"; }
            case STAGE_3 -> { return "Launched"; }
            case STAGE_4 -> { return "Awaiting Draft Loan Documents"; }
            case STAGE_5 -> { return "Draft Loan Documents Complete"; }
            case STAGE_6 -> { return "Final Loan Documentation Complete"; }
            case STAGE_7 -> { return "Awaiting Closing Memo"; }
            case STAGE_8 -> { return "Awaiting Closing"; }
            case STAGE_9 -> { return "Event Closed"; }
            default -> throw new IllegalStateException("Event Stage is not defined.");
        }
    }

}