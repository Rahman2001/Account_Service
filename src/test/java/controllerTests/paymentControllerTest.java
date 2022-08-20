package controllerTests;

import account.AccountServiceApplication;
import account.Controllers.PaymentController;
import account.Domain.PaymentsToEmployee;
import account.Domain.User;
import account.Repositories.PaymentRepo;
import account.Repositories.UserRepo;
import account.Security.EmployeeAuthentication.EmployeePostOrGetAuthentication;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {AccountServiceApplication.class})
public class paymentControllerTest {

    @Mock
    private PaymentRepo paymentRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    private JSONArray jsonArray;

    private PaymentsToEmployee paymentsToEmployee;

    @BeforeEach
    public void createJsonArray() {
        Map<String, Object> request = new LinkedHashMap<>(); //data1 for stubbing
        request.put("employee", "rahman@acme.com");
        request.put("period", "01-2021");
        request.put("salary", 123456);

        Map<String, Object> request2 = new LinkedHashMap<>(); // data2 for stubbing
        request2.put("employee", "rahym@acme.com");
        request2.put("period", "01-2022");
        request2.put("salary", 123478);

        List<Map<String, Object>> listOfMaps = List.of(request, request2);
        this.jsonArray = new JSONArray(listOfMaps); //converting our data into json array so we could send it as data request in our controller method

       this.paymentsToEmployee = PaymentsToEmployee.builder().id(13L).employee("Rahman")
               .salary("123478").period("01-2022").build(); // data for stubbing

        this.mockMvc = MockMvcBuilders.standaloneSetup(this.paymentController).build(); //we want to show to testing framework which controller should be tested and what should be injected into that controller.
    }

    @Test
    @DisplayName("uploadPayrollsMethod (200 response)")
    public void uploadPayrollsSuccessfulTest() throws Exception {
        when(this.paymentRepo.save(any(PaymentsToEmployee.class))).thenReturn(this.paymentsToEmployee);
        when(this.userRepo.findByEmailIgnoreCase(anyString())).thenReturn(User.builder().build());
        when(this.paymentRepo.findAllByEmail(anyString())).thenReturn(new ArrayList<>());

        this.mockMvc.perform(post("/api/acct/payments").contentType(MediaType.APPLICATION_JSON)
                .content(this.jsonArray.toString())).andExpect(status().isOk()).andExpect(jsonPath("status").value("Added successfully!"));

    }

    @Test
    @DisplayName("uploadPayrollsMethod (400 response)")
    public void uploadPayrollsUnsuccessfulTest() throws Exception{
        when(this.userRepo.findByEmailIgnoreCase(anyString())).thenReturn(null);

        this.mockMvc.perform(post("/api/acct/payments").contentType(MediaType.APPLICATION_JSON)
                .content(this.jsonArray.toString())).andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("changeSalaryMethod")
    public void changeSalarySuccessfulTest() throws Exception {
        User userForStubbing = User.builder().id(13L).email("rahmanrejepov@acme.com").build();

        when(this.userRepo.findByEmailIgnoreCase(anyString())).thenReturn(userForStubbing);
        when(this.paymentRepo.findAllByEmail(anyString())).thenReturn(new ArrayList<>());
        when(this.paymentRepo.updateEmployeeSalary(anyString(), anyString(), anyString())).thenReturn(1);

        this.mockMvc.perform(put("/api/acct/payments").contentType(MediaType.APPLICATION_JSON)
                .content(String.valueOf(this.jsonArray.get(0)))).andExpect(status().isOk())
                .andExpect(jsonPath("status").value("Updated successfully!"));
    }
}