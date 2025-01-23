package com.westmonroe.loansyndication.model.deal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "Deal Members", description = "Model for a Lamina deal members.")
public class DealMembers implements Serializable {

    private static final long serialVersionUID = 1L;

    private Deal deal;
    private List<User> users;

}