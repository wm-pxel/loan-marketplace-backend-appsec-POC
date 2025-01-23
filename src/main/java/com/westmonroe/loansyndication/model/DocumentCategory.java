package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Document Category", description = "Model for a Lamina document category.")
public class DocumentCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Integer order;
    private String dealDocumentFlag;

    public DocumentCategory(Long id) {
        this.id = id;
    }

}