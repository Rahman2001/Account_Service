package account.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import account.jpaConverter.ListToString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1234567L;

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column
    private String name;

    @NotBlank
    @Column
    private String lastname;

    @NotBlank
    @Email(regexp = "[a-z0-9._%+-]+@acme.com",
            flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid employee format! Must be ___@acme.com")
    @Column
    private String email;

    @NotBlank
    @Column
    private String password;

    @Column
    @Convert(converter = ListToString.class)
    private List<String> roles;

    @OneToMany(mappedBy = "user")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<PaymentsToEmployee> payments;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return password;
    }

}
