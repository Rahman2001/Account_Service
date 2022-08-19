package account.Controllers;

import account.Domain.PaymentsToEmployee;
import account.Repositories.PaymentRepo;
import account.Repositories.UserRepo;
import account.Security.EmployeeAuthentication.EmployeePostOrGetAuthentication;
import account.Service.UserDetailsImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PaymentController { // Accountant can 1)access to the employee's payrolls,
                                 //2)upload payrolls, 3)update payment information
                                 //User has access ONLY to first option but Admin doesn't have access to any of them
                                 //!!Accountant doesn't have to be authorized in id to get info from each api

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PaymentRepo paymentRepo;


    @PostMapping("/acct/payments") //uploads payrolls
    public ResponseEntity<Map<String, String>> uploadPayrolls(@RequestBody PaymentsToEmployee[] paymentsToEmployeeJson,
                                                              @Autowired EmployeePostOrGetAuthentication employeeAuth) throws ParseException {
        Map<String, String> responses = new HashMap<>();
        employeeAuth.setPaymentRepo(this.paymentRepo);
        employeeAuth.setUserRepo(this.userRepo);

        for(PaymentsToEmployee paymentsToEmployeeData : paymentsToEmployeeJson) {
            employeeAuth.setPaymentsToEmployee(paymentsToEmployeeData);                                              // checks if @RequestBody data is correct according
                                                                                                                     // the following criteria: 1) employee email must exist in database,
                                                                                                                     //                         2) new period should be future of existing period in database
                                                                                                                     //                         3) salary cannot be negative value.
            if (employeeAuth.isAuthenticated("post")) {
                this.paymentRepo.save(employeeAuth.getAuthenticatedEmployee());
                responses.put("status", "Added successfully!");
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
        }
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/acct/payments") //changes the salary of a specific user
    public ResponseEntity<Map<String, String>> changeSalary(@RequestBody PaymentsToEmployee paymentsToEmployeeJson, @Autowired EmployeePostOrGetAuthentication employeeAuth) throws JsonProcessingException, ParseException {
        Map<String, String> responses = new HashMap<>();
        int updatedRow;
        employeeAuth.setPaymentRepo(this.paymentRepo);
        employeeAuth.setUserRepo(this.userRepo);
        employeeAuth.setPaymentsToEmployee(paymentsToEmployeeJson);                                              // checks if @RequestBody data is correct according
                                                                                                                      // the following criteria: 1) employee email must exist in database,
                                                                                                                      //                         2) new period should be future of existing period in database
                                                                                                                      //                         3) salary cannot be negative value.
        if (employeeAuth.isAuthenticated("put")) {
            PaymentsToEmployee paymentsToEmployee = employeeAuth.getAuthenticatedEmployee();
            updatedRow = this.paymentRepo.updateEmployeeSalary(paymentsToEmployee.getUser().getEmail(), paymentsToEmployee.getPeriod(), paymentsToEmployee.getSalary());
            responses.put("status", "Updated successfully!");
            return updatedRow == 1 ? ResponseEntity.ok(responses) : ResponseEntity.badRequest().build();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

   @GetMapping("/empl/payment")
   public ResponseEntity<?> getEmployeePayrolls(@RequestParam(name = "period",required = false) String period,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) throws ParseException {
       SimpleDateFormat formatOfDate = new SimpleDateFormat("MM-yyyy");
       SimpleDateFormat newFormatOfData = new SimpleDateFormat("MMMMMMMM-yyyy");

       if(userDetails != null) {
           if (period != null) {
               if(Integer.parseInt(period.split("-")[0]) <= 12) { // we check if period has a valid month date
                   PaymentsToEmployee paymentsToEmployeePayroll = this.paymentRepo.findPaymentsByEmployeePeriod(userDetails.getUsername().toLowerCase(), period);
                   if (paymentsToEmployeePayroll != null) { // we check if there is any payroll of the employee with such email in database

                       Date date = formatOfDate.parse(period);
                       period = newFormatOfData.format(date);

                       Map<String, String> response = new LinkedHashMap<>();
                       response.put("name", paymentsToEmployeePayroll.getUser().getName());
                       response.put("lastname", paymentsToEmployeePayroll.getUser().getLastname());
                       response.put("period", period);
                       response.put("salary", paymentsToEmployeePayroll.getSalary());
                       return ResponseEntity.ok(response);
                   }
                   return ResponseEntity.ok(new LinkedHashMap<>());
               }
               throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
           }

           List<PaymentsToEmployee> paymentsToEmployeePayrollList = this.paymentRepo.findListOfPeriodsOfEmployee(userDetails.getUsername().toLowerCase());
           if (!paymentsToEmployeePayrollList.isEmpty()) { // we check if there is a list of payrolls of employee with such email
               List<Map<String, String>> response = new ArrayList<>();

               paymentsToEmployeePayrollList.forEach(eachPaymentsToEmployee -> {
                   Map<String, String> map = new LinkedHashMap<>();
                   map.put("name", eachPaymentsToEmployee.getUser().getName());
                   map.put("lastname", eachPaymentsToEmployee.getUser().getLastname());

                   Date date;
                   try {
                       date = formatOfDate.parse(eachPaymentsToEmployee.getPeriod());
                   } catch (ParseException e) {
                       throw new RuntimeException(e);
                   }

                   map.put("period", newFormatOfData.format(date));
                   map.put("salary", eachPaymentsToEmployee.getSalary());
                   response.add(map);
               });

               return ResponseEntity.ok(response);
           }
           return ResponseEntity.ok(new ArrayList<LinkedHashMap<String, String>>());
       }
       return ResponseEntity.status(401).build();
   }
}