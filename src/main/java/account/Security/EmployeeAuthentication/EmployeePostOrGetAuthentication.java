package account.Security.EmployeeAuthentication;

import account.Domain.PaymentsToEmployee;
import account.Domain.User;
import account.Repositories.PaymentRepo;
import account.Repositories.UserRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;


public class EmployeePostOrGetAuthentication {
    private String email;
    private String period;
    private String salary;
    private final PaymentRepo paymentRepo;
    private final UserRepo userRepo;
    private PaymentsToEmployee paymentsToEmployee;
    private User user;


    public EmployeePostOrGetAuthentication(PaymentsToEmployee paymentsToEmployee, PaymentRepo paymentRepo, UserRepo userRepo) throws JsonProcessingException {
        this.paymentsToEmployee = paymentsToEmployee;
        this.email = paymentsToEmployee.getEmployee();
        this.salary = paymentsToEmployee.getSalary();
        this.period = paymentsToEmployee.getPeriod();
        this.paymentRepo = paymentRepo;
        this.userRepo = userRepo;
    }
    private boolean checkEmail(String email) { // paymentsToEmployee's paymentsToEmployee must exist in our database.
        this.user = this.userRepo.findByEmailIgnoreCase(email);
        return this.user != null;
    }

    private boolean checkPeriod(String period, String requestType) throws ParseException {

        if(Integer.parseInt(period.split("-")[0]) <= 12) { // we check if date of month is a valid data.

            if(requestType.equalsIgnoreCase("post")) {
                List<PaymentsToEmployee> paymentsToEmployeeListByEmail = this.paymentRepo.findAllByEmail(this.email);

                if (!paymentsToEmployeeListByEmail.isEmpty() || this.user != null) { // we check if there is either paymentsToEmployee or a user with such email
                    for (PaymentsToEmployee eachPaymentsToEmployee : paymentsToEmployeeListByEmail) { // whether there is such paymentsToEmployee or not, we can be satisfied that there is a user with such email
                        if (eachPaymentsToEmployee.getPeriod().equalsIgnoreCase(period)) { // in case, we have an paymentsToEmployee, the period should not be the same with its records in database
                            return false;
                        }
                    }

                    return true;
                }
            }else return requestType.equalsIgnoreCase("put");
        }
        return false; // if the period is not valid, then return false;
    }

    public boolean isAuthenticated(String requestType) throws ParseException { // uses all check lists above for verification of data provided by a client
        if(requestType.equalsIgnoreCase("post")){
            return this.checkEmail(this.email) && this.checkPeriod(this.period, requestType);
        }
        if(requestType.equalsIgnoreCase("put")) {
            return this.checkEmail(this.email) && this.checkPeriod(this.period, requestType);
        }
        return false;
    }

    public PaymentsToEmployee getAuthenticatedEmployee() {
        List<PaymentsToEmployee> paymentsToEmployeeFromDatabase = this.paymentRepo.findAllByEmail(this.email);

        if(!paymentsToEmployeeFromDatabase.isEmpty()) {
            this.paymentsToEmployee.setUser(paymentsToEmployeeFromDatabase.get(0).getUser());
            return this.paymentsToEmployee;
        }
        if(this.user != null) {
            this.paymentsToEmployee.setUser(this.user);
            return this.paymentsToEmployee;
        }
        return null;
    }
}
