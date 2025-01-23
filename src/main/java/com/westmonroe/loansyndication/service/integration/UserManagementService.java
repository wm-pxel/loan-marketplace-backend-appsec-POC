package com.westmonroe.loansyndication.service.integration;

import com.westmonroe.loansyndication.model.Role;
import com.westmonroe.loansyndication.model.User;
import com.westmonroe.loansyndication.model.integration.RoleDto;
import com.westmonroe.loansyndication.model.integration.UserDto;
import com.westmonroe.loansyndication.model.integration.mapper.UserMapper;
import com.westmonroe.loansyndication.service.DefinitionService;
import com.westmonroe.loansyndication.service.InstitutionService;
import com.westmonroe.loansyndication.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private final InstitutionService institutionService;
    private final UserService userService;
    private final DefinitionService definitionService;

    public UserManagementService(InstitutionService institutionService, UserService userService, DefinitionService definitionService) {
        this.institutionService = institutionService;
        this.userService = userService;
        this.definitionService = definitionService;
    }

    public List<RoleDto> getRoles() {

        List<Role> roles = definitionService.getAllRoles();
        List<RoleDto> roleDtos = new ArrayList<>();

        for ( Role role : roles ) {
            roleDtos.add(new RoleDto(role.getId(), role.getCode(), role.getDescription()));
        }

        return roleDtos;
    }

    public List<UserDto> getUsersForInstitutionUid(String institutionUid) {
        List<User> users = userService.getUsersForInstitutionUid(institutionUid);

        UserMapper userMapper = new UserMapper(institutionService);

        List<UserDto> userDtos = new ArrayList<>();

        for ( User user : users ) {
            userDtos.add(userMapper.userToUserDto(user));
        }

        return userDtos;
    }

    public UserDto getUserByUid(String uid) {
        return getUserByUid(uid, false);
    }

    public UserDto getUserByUid(String uid, boolean fetchRoles) {

        User user = userService.getUserByUid(uid);

        UserMapper userMapper = new UserMapper(institutionService);
        UserDto userDto = userMapper.userToUserDto(user);

        // Only get roles if flag is true.
        if ( fetchRoles ) {
            userDto.setRoles(getRolesForUserUid(user.getUid()));
        }

        return userDto;
    }

    public List<RoleDto> getRolesForUserUid(String userUid) {
        List<Role> roles = userService.getRolesForUserUid(userUid);
        return roles.stream().map(r -> new RoleDto(r.getId(), r.getCode(), r.getName())).collect(Collectors.toList());
    }

    public UserDto saveRoleForUser(UserDto userDto, RoleDto roleDto, User currentUser) {
        return saveRoleForUser(userDto, roleDto, currentUser, false);
    }

    public UserDto saveRoleForUser(UserDto userDto, RoleDto roleDto, User currentUser, boolean fetchRoles) {

        // Using the UserService, save the role for the user.
        userService.saveRoleForUser(userDto.getUid(), roleDto.getId(), currentUser);

        return getUserByUid(userDto.getUid(), fetchRoles);
    }

    public void deleteRoleForUser(String userUid, Long roleId, User currentUser) {
        userService.deleteRoleForUser(userUid, roleId, currentUser);
    }

    @Transactional
    public UserDto save(UserDto userDto) {

        /*
         *  Convert UserDto object to User before saving.
         */
        UserMapper userMapper = new UserMapper(institutionService);
        User user = userMapper.userDtoToUser(userDto);

        user = userService.save(user);

        return getUserByUid(user.getUid(), true);
    }

    public void update(UserDto userDto) {

        /*
         *  Convert UserDto object to User before updating.
         */
        UserMapper userMapper = new UserMapper(institutionService);
        User user = userMapper.userDtoToUser(userDto);

        userService.update(user);
    }

    public void delete(UserDto userDto) {

        try {
            userService.deleteByInstitutionUidAndUserUid(userDto.getInstitution().getUid(), userDto.getUid());
        } catch ( Exception e ) {

            /*
             *  We got an error trying to delete, so make the user inactive and perform an update.
             */
            userDto.setActive("N");
            update(userDto);

        }

    }

}