package account.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Builder
@AllArgsConstructor
@Entity
@NoArgsConstructor
@Data
public class PaymentsToEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;
    @Column
    private String period;
    @Column
    private String salary;

    @Transient
    @Email(regexp = "[a-z0-9._%+-]+@acme.com",
            flags = Pattern.Flag.CASE_INSENSITIVE, message = "Invalid employee format! Must be ___@acme.com")
    private String employee;

    @ManyToOne
    @JoinColumns(value = {@JoinColumn(name = "name", referencedColumnName = "name"),
    @JoinColumn(name = "lastname", referencedColumnName = "lastname"),
    @JoinColumn(name = "employee", referencedColumnName = "email")})
    private User user;

    public void setSalary(Long salary) {
        if(salary >= 0) {
            Double salaryWithCents = salary/100.00;
            String[] salaryParts = String.valueOf(salaryWithCents).split("\\.");
            String salaryWholeNumber = salaryParts[0];
            String salaryDecimalNumber = salaryParts[1];
            this.salary = salaryWholeNumber + " dollar(s) " + salaryDecimalNumber + " cent(s)";

        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A salary cannot be of negative value!");
        }
    }
}