package com.westmonroe.loansyndication.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.deal.DealFacility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventDealFacility implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Event event;

    private DealFacility dealFacility;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

    public EventDealFacility(Event event, DealFacility dealFacility) {
        this.event = event;
        this.dealFacility = dealFacility;
    }

}