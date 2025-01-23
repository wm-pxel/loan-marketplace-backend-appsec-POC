package com.westmonroe.loansyndication.utils;

public enum ParticipantStepEnum {

    STEP_1(1), STEP_2(2), STEP_3(3), STEP_4(4), STEP_5(5), STEP_6(6),
    STEP_7(7), STEP_8(8), STEP_9(9), STEP_10(10), STEP_11(11), STEP_12(12);

    private int order;

    ParticipantStepEnum(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public String getName() {
        switch(this) {
            case STEP_1 -> { return "Added to Deal as Draft"; }
            case STEP_2 -> { return "Invited to Deal"; }
            case STEP_3 -> { return "Indicated Interest"; }
            case STEP_4 -> { return "Awaiting Deal Launch"; }
            case STEP_5 -> { return "Full Deal Access Provided"; }
            case STEP_6 -> { return "Committed"; }
            case STEP_7 -> { return "Allocated"; }
            case STEP_8 -> { return "Awaiting Participant Certificate"; }
            case STEP_9 -> { return "Participant Certificate Provided"; }
            case STEP_10 -> { return "Participant Certificate Signed"; }
            case STEP_11 -> { return "Declined"; }
            case STEP_12 -> { return "Removed"; }
            default -> throw new IllegalStateException("Participant step is not defined.");
        }
    }

}