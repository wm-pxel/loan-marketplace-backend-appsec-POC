package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_TRANSFER_TYPE;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Document Batch", description = "Model for a Lamina document batch.")
public class DocumentBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotEmpty(message = "Deal External ID cannot be empty or null")
    @Size(max = 55, message = "Deal External ID cannot be greater than 55 characters.")
    private String dealExternalId;

    @NotEmpty(message = "Transfer type cannot be empty or null")
    @Pattern(regexp = REGEX_TRANSFER_TYPE, message = "Transfer type must have a value of U for upload or D for download")
    private String transferType;

    @ReadOnlyProperty
    private String dealId;

    @ReadOnlyProperty
    private OffsetDateTime processStartDate;

    @ReadOnlyProperty
    private OffsetDateTime processEndDate;

    @ReadOnlyProperty
    private Long createdById;

    @ReadOnlyProperty
    private String createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @Valid
    @NotNull(message = "Details cannot be null")
    private List<DocumentBatchDetail> details;

}