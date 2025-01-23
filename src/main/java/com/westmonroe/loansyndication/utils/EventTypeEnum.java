package com.westmonroe.loansyndication.utils;

public enum EventTypeEnum {

    ORIGINATION(1L, "Origination"),
    VOTING(2L, "Voting"),
    SIMPLE_RENEWAL(3L, "Simple Renewal"),
    COMPLEX_RENEWAL(4L, "Complex Renewal");

    private final Long id;
    private final String name;

    EventTypeEnum(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static EventTypeEnum valueOfName(String name) {

        for ( EventTypeEnum ete : values() ) {
            if ( ete.name.equalsIgnoreCase(name) ) {
                return ete;
            }
        }

        return null;
    }

}