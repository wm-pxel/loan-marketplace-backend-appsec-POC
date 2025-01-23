package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InitialLender implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String lenderName;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private String updatedDate;

    private String active;

}