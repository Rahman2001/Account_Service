package account.Security.PasswordOperations;

import account.Service.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Component
public class PasswordSecurityChecklist {

    private final String[] breachedPasswords = {"PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
            "PasswordForApril", "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
            "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"};
    private boolean isNotBreached;
    private boolean is12LengthOrMore;
    private boolean isNewPassword;

    private boolean isNotBreached(String newPassword) { //if a new password matches with breached ones then throw 400 error
        return this.isNotBreached = Arrays.stream(this.breachedPasswords).noneMatch(x -> x.equalsIgnoreCase(newPassword));
    }
    private boolean is12LengthOrMore(String newPassword) { //if a password isn't at least 12 length then throw 400 error
        if(!newPassword.isBlank()) {
            return this.is12LengthOrMore = newPassword.length() >= 12;
        }else {
            return this.is12LengthOrMore = false;
        }
    }
    private boolean isNewPassword(UserDetailsImpl userDetails, String newPassword, PasswordEncoder encoder) { //if a new password matches with the old one, then throw 400 error
        return this.isNewPassword = !(encoder.matches(newPassword, userDetails.getPassword()));
    }

    public boolean hasBasicSecurity(String api, String password, @Nullable UserDetailsImpl userDetails,
                                    PasswordEncoder encoder) {
        if(api.equals("/signup")) {
            return this.isNotBreached(password) && this.is12LengthOrMore(password);
        }
        if(api.equals("/changepass") && userDetails != null) {
            return this.isNotBreached(password) && this.is12LengthOrMore(password)
                    && this.isNewPassword(userDetails, password, encoder);
        }
        return false;
    }

    public ResponseStatusException falseReason() {
        if(!this.isNotBreached) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        }else if(!this.is12LengthOrMore) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password length must be 12 chars minimum!");
        }else if(!this.isNewPassword) {
            return new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        }
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong!");
    }
}
