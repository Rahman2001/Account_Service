package account.Controllers;

import account.Domain.User;
import account.Repositories.UserRepo;
import account.Service.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user")
@PreAuthorize("hasAuthority('ADMINISTRATOR')")
public class AdminController { //Admin can  1)change user roles, 2)delete a user, 3)obtain information about all users (information should not be sensitive)
                               //The roles are divided into 2 groups: administrative (Administrator) and business users (Accountant, User).
                               //A user can be either from the administrative or business group. A user with an administrative role can not have access to business functions and vice versa.
    @Autowired
    private UserRepo userRepo;

    @PutMapping("/role")
    public ResponseEntity<User> changeRole(@AuthenticationPrincipal UserDetailsImpl admin, @RequestBody String userRoleData) throws JsonProcessingException { // changes user roles.
        ObjectMapper objectMapper = new ObjectMapper();
        Map userMap = objectMapper.readValue(userRoleData, Map.class);
        String requestedUserEmail = (String) userMap.get("user");

        if(this.userRepo.findByEmailIgnoreCase(requestedUserEmail) != null) { // a user must exist in database, otherwise 400 code error is thrown

            if (!requestedUserEmail.equalsIgnoreCase(admin.getUsername())) { // sent JSON data about user role change cannot be performed on administrator.
                String role = (String) userMap.get("role");
                String[] currentRolesOfUser;

                if(this.userRepo.hasAnyRoles(requestedUserEmail)) {
                    currentRolesOfUser = this.userRepo.getRolesOfUser(requestedUserEmail).split(",");
                }else{
                    currentRolesOfUser = null;
                }

                String[] updatedRolesOfUser = null;

                boolean isRemoveOperation = ((String) userMap.get("operation")).equalsIgnoreCase("remove");
                boolean isGrantOperation = ((String) userMap.get("operation")).equalsIgnoreCase("grant");

                if (currentRolesOfUser != null) { // if the user has roles already, then continue below operations

                    if (isRemoveOperation) { // if operation will be to remove a role from the user, then perform below command

                        if(currentRolesOfUser.length == 1) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
                        }
                        updatedRolesOfUser = Arrays.stream(currentRolesOfUser).filter(x -> !x.equalsIgnoreCase(role)).findAny().stream().toArray(String[]::new);

                    } else if (isGrantOperation) { // if operation will be to grant a role to the user, then perform below command
                        List<String> listOfUpdatedRoles = Arrays.stream(currentRolesOfUser).collect(Collectors.toList());
                        listOfUpdatedRoles.add(role);
                        updatedRolesOfUser = listOfUpdatedRoles.toArray(String[]::new);
                    }

                } else {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
                }

                if(this.userRepo.updateUserRole(requestedUserEmail, updatedRolesOfUser) == 1) { // we update the view of information about the user roles. Roles must appear like this: "ROLE_USER", "ROLE_ADMIN".
                    User tempUser = this.userRepo.findByEmailIgnoreCase(requestedUserEmail);
                    List<String> roleAttributeAdded = tempUser.getRoles().stream().map(x -> x = "ROLE_" + x).collect(Collectors.toList());
                    tempUser.setRoles(roleAttributeAdded);
                    return ResponseEntity.ok(tempUser);
                }
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<Map<String, Object>> deleteUser(@AuthenticationPrincipal UserDetailsImpl admin, @PathVariable("email") String email) {
        Map<String, Object> map = new LinkedHashMap<>();
        User userToDelete = this.userRepo.findByEmailIgnoreCase(email);

        if(email.equalsIgnoreCase(admin.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
        }
        else if(userToDelete == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!");
        }
        map.put("user", email);
        map.put("status", "Deleted successfully!");

        return this.userRepo.deleteUser(email) == 1 ? ResponseEntity.ok(map) : ResponseEntity.badRequest().build();
    }

    @GetMapping("/role")
    public List<User> getAllUsers() {
        return this.userRepo.getAllUsersSortedById();
    }
}
