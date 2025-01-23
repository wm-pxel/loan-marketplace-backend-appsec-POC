package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.westmonroe.loansyndication.validation.UniqueInstitutionName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Institution implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long id;

    @Pattern(regexp = REGEX_UUID, message = "The id must be in valid UUID format.")
    private String uid;

    @NotNull(message = "The Institution Name cannot be null.")
    @Size(min = 5, message = "The institution name must be at least 5 characters")
    @Size(max = 80, message = "The institution name must be no more than 80 characters")
    @UniqueInstitutionName
    private String name;

    private List<User> users;

    private String brandName;

    private String communityExtension;

    private String communityName;

    private String communityNetworkID;

    private String lookupKey;

    private String owner;

    private BillingCode billingCode;

    @Pattern(regexp = REGEX_PERMISSION_SET, message = "The permission set can only be Lead and Participant or Participant Only.")
    private String permissionSet;

    @Pattern(regexp = REGEX_YN, message = "The active flag can only be Y or N.")
    private String active;

    private Long dealCount;

    private Long memberCount;

    @Pattern(regexp = REGEX_YN, message = "The SSO indicator flag can only be Y or N.")
    private String ssoFlag;

    public Institution(Long id) {
        this.id = id;
    }

    public Institution(String uid) {
        this.uid = uid;
    }

    public Institution(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}