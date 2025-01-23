package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal User Confidentiality Agreement", description = "Model for a Lamina deal user confidentiality agreement.")
public class UserEua implements Serializable {

    private Long userId;
    private EndUserAgreement endUserAgreement;
    private OffsetDateTime agreementDate;
}
