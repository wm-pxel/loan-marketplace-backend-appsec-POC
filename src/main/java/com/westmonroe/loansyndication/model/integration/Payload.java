package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Payload", description = "Model for payload in the integration data.")
public class Payload implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /*
     *  This hidden field is used to launch the File Service Lambda.
     */
    @Hidden
    private Long batchId;

    @Valid
    @NotNull(message = "Marketplace data cannot be null")
    private MarketplaceData marketplaceData;

    private Map<String, Object> unsupportedData;

    @JsonIgnore
    private Long createdById;

    @ReadOnlyProperty
    private String createdBy;

    @ReadOnlyProperty
    private String createdDate;

}