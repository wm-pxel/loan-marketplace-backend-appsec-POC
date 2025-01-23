package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.westmonroe.loansyndication.validation.ValidCategoryName;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "Deal Batch Detail", description = "Model for a Lamina document batch detail.")
public class DocumentBatchDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long Id;

    private Long documentBatchId;

    @Hidden
    @ReadOnlyProperty
    private DealDocumentDto dealDocument;

    @NotEmpty(message = "Document External ID cannot be empty or null")
    @Size(max = 55, message = "Document External ID cannot be greater than 55 characters.")
    private String documentExternalId;

    @NotEmpty(message = "Document url cannot be empty or null")
    @Size(max = 255, message = "Document url cannot be greater than 255 characters.")
    private String url;

    @NotEmpty(message = "Display name cannot be empty or null")
    @Size(max = 100, message = "Display name cannot be greater than 100 characters.")
    private String displayName;

    @NotEmpty(message = "File extension cannot be empty or null")
    @Size(max = 40, message = "File extension cannot be greater than 40 characters.")
    private String extension;

    @NotEmpty(message = "Category name cannot be empty or null")
    @ValidCategoryName
    private String category;

    private String salesforceId;

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

}