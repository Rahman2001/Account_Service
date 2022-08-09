package account.Controllers;

import account.Domain.User;
import account.Repositories.UserRepo;
import account.Security.PasswordOperations.PasswordSecurityChecklist;
import account.Service.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserAuthController { //User can 1)register on the service, 2)change his/her password
                                  //Accountant and Admin also have access to these options
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder encoder;

    @PostMapping("/auth/signup")
    public User response(@RequestBody @Valid User user, @Autowired PasswordSecurityChecklist passwordCheck) throws JsonProcessingException {
        if (userRepo.findByEmailIgnoreCase(user.getEmail()) != null) { //if same user found in database, then throw bad request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
        }

        if(passwordCheck.hasBasicSecurity("/signup", user.getPassword(), null, null)) { // check by basic security list
            user.setPassword(this.encoder.encode(user.getPassword()));

            if(user.getRoles() == null) { // if a user does not have any role,

                if(((List<User>)this.userRepo.findAll()).isEmpty()){ // if a user is a first registered user, then that user is Administrator
                    user.setRoles(List.of("ADMINISTRATOR"));
                }else {
                    user.setRoles(List.of("USER")); // else a user is User;
                }
            }
            user = userRepo.save(user);
            User user1 = User.builder().id(user.getId()).email(user.getEmail()).name(user.getName())
                    .lastname(user.getLastname()).roles(user.getRoles()).build();

            List<String> updatedList = user1.getRoles().stream().map(x -> x = "ROLE_" + x).collect(Collectors.toList());
            user1.setRoles(updatedList);

            return user1; // otherwise, return requested user details by sending 200 (OK) response
        }else {
            throw passwordCheck.falseReason();
        }
    }

    @PostMapping("/auth/changepass")
    public ResponseEntity<Map<String, String>> response(@AuthenticationPrincipal @Nullable UserDetailsImpl userDetailsImpl,
                                   @RequestBody String newPassword,
                                   @Autowired PasswordSecurityChecklist passwordCheck) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map = objectMapper.readValue(newPassword, Map.class);
        newPassword = map.get("new_password");

        if(userDetailsImpl != null) {

            if (passwordCheck.hasBasicSecurity("/changepass", newPassword, userDetailsImpl, this.encoder)) { //check by basic security list
                                                                                                                 // otherwise, update a password of a user and return positive response about that
                Map<String, String> response = new HashMap<>();
                response.put("email", userDetailsImpl.getEmail().toLowerCase());
                response.put("status", "The password has been updated successfully");

                return userRepo.updatePassword(userDetailsImpl.getEmail(), encoder.encode(newPassword)) == 1 ?
                        ResponseEntity.ok(response) : ResponseEntity.badRequest().build();
            } else {
                throw passwordCheck.falseReason();
            }
        }else {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}