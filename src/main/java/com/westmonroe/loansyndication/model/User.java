package com.westmonroe.loansyndication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.westmonroe.loansyndication.validation.UniqueUserEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.westmonroe.loansyndication.utils.Constants.REGEX_EMAIL;
import static com.westmonroe.loansyndication.utils.Constants.REGEX_YN;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "User", description = "Model for a Lamina user.")
public class User implements UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Long id;

    private String uid;

    private Institution institution;

    @Size(max = 40, message = "First name cannot be greater than 40 characters.")
    private String firstName;

    @Size(max = 80, message = "Last name cannot be greater than 80 characters.")
    private String lastName;

    private String fullName;

    @Pattern(regexp = REGEX_EMAIL, message = "The email format is not valid")
    @UniqueUserEmail
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private InviteStatus inviteStatus;

    @Pattern(regexp = REGEX_YN, message = "The active flag can only be Y or N.")
    private String active;

    private String createdDate;

    private List<Role> roles;

    @Pattern(regexp = REGEX_YN, message = "The System User flag can only be Y or N.")
    private String systemUser;

    public User(Long id) {
        this.id = id;
    }

    public User(String uid) {
        this.uid = uid;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> authorities = new ArrayList<>();

        if ( roles != null ) {
            roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode())).forEach(authorities::add);
        }

        return authorities;
    }

    @Override
    public String getUsername() {
        return uid;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active.equals("Y");
    }

    public String getFullName() {
        return this.getFirstName() + " " + this.lastName;
    }

}