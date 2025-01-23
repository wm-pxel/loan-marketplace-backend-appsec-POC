package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.DocumentCategory;
import com.westmonroe.loansyndication.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal Document", description = "Model for a Lamina deal document.")
public class DealDocument implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Deal deal;

    private String displayName;

    private String documentName;

    private DocumentCategory category;

    private String documentType;

    private String description;

    private String source;

    @Size(max = 55, message = "Document External ID cannot be greater than 55 characters.")
    private String documentExternalId;

    @ReadOnlyProperty
    private User createdBy;

    @ReadOnlyProperty
    private String createdDate;

    @ReadOnlyProperty
    private User updatedBy;

    @ReadOnlyProperty
    private String updatedDate;

}