package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EndUserAgreement implements Serializable {

    private static final long serialVersionUid = 1L;

    private Long id;

    private String content;

    private BillingCode billingCode;

    @ReadOnlyProperty
    private String createdDate;
    public EndUserAgreement(Long id){ this.id = id; }
}
