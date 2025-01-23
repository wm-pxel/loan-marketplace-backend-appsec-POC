package com.westmonroe.loansyndication.model.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.validation.UniqueUserEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_EMAIL;
import static com.westmonroe.loansyndication.utils.Constants.REGEX_YN;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "UserDto", description = "Model for a Lamina user.")
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uid;

    private InstitutionDto institution;

    @Size(max = 40, message = "First name cannot be greater than 40 characters.")
    private String firstName;

    @Size(max = 80, message = "Last name cannot be greater than 80 characters.")
    private String lastName;

    @Pattern(regexp = REGEX_EMAIL, message = "The email format is not valid")
    @UniqueUserEmail(message = "The supplied email is already being used by another user. The user email must be unique.")
    private String email;

    @Pattern(regexp = REGEX_YN, message = "The active flag can only be Y or N.")
    private String active;

    private List<RoleDto> roles = new ArrayList<>();

}