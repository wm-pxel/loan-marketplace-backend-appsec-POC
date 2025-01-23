package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.deal.Deal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.ReadOnlyProperty;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_YN;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Email Notification", description = "Model for Lamina email notifications.")
public class EmailNotification {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Deal deal;

    @NotNull
    private String emailTypeCd;

    @NotEmpty
    @Pattern(regexp = REGEX_YN, message = "The processedInd flag must be Y or N.")
    private String processedInd;

    @ReadOnlyProperty
    private String createdDate;

    private String templateDataJson;

}
