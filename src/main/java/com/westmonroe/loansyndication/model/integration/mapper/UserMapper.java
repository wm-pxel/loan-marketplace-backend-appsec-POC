package com.westmonroe.loansyndication.model.integration.mapper;

import com.westmonroe.loansyndication.model.Institution;
import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.InstitutionDto;
import com.westmonroe.loansyndication.model.integration.RoleDto;
import com.westmonroe.loansyndication.model.integration.UserDto;
import com.westmonroe.loansyndication.service.InstitutionService;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    private final InstitutionService institutionService;

    public UserMapper(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }

    public User userDtoToUser(UserDto userDto) {

        User user = new User();

        Institution institution = institutionService.getInstitutionByUid(userDto.getInstitution().getUid());
        user.setInstitution(institution);

        user.setUid(userDto.getUid());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setActive(userDto.getActive());

        /*
         *  If there were any roles then add them to the user object.
         */
        if ( userDto.getRoles() != null && !userDto.getRoles().isEmpty() ) {

            List<Role> roles = userDto.getRoles().stream().map(r -> new Role(r.getId(), r.getCode(), r.getName(), null, null)).collect(Collectors.toList());
            user.setRoles(roles);

        }

        return user;
    }

    public UserDto userToUserDto(User user) {

        UserDto userDto = new UserDto();

        InstitutionDto institutionDto = new InstitutionDto();
        institutionDto.setUid(user.getInstitution().getUid());
        institutionDto.setName(user.getInstitution().getName());
        institutionDto.setActive(user.getInstitution().getActive());
        userDto.setInstitution(institutionDto);

        userDto.setUid(user.getUid());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setActive(user.getActive());

        /*
         *  If there were any roles then add them to the user object.
         */
        if ( user.getRoles() != null && !user.getRoles().isEmpty() ) {

            List<RoleDto> roleDtos = user.getRoles().stream().map(r -> new RoleDto(r.getId(), r.getCode(), r.getName())).collect(Collectors.toList());
            userDto.setRoles(roleDtos);

        }

        return userDto;
    }

}