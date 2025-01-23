package com.westmonroe.loansyndication.model.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventType implements Serializable {

    private static final long serialVersionUID = 1L;

    public EventType(Long id) {
        this.id = id;
    }

    private Long id;
    private String name;

}