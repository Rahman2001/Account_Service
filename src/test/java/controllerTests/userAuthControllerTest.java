package controllerTests;

import account.AccountServiceApplication;
import account.Domain.User;
import account.Repositories.UserRepo;
import account.Service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {AccountServiceApplication.class})
@AutoConfigureMockMvc
public class userAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepo userRepo;

    private User tempUser;

    @BeforeEach
    public void createData() {
        this.tempUser = User.builder().name("Aynura").lastname("Rejepova")
                .email("aynur@acme.com").password("dolceGabana77").roles(List.of("ADMINISTRATOR")).build();
    }

    @Test
    public void signupTestUnsuccessful() throws Exception { //test of API: "/api/auth/signup"
        when(this.userRepo.findByEmailIgnoreCase(anyString())).thenReturn(this.tempUser);

        this.mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(this.tempUser))).andExpect(status().isBadRequest());
    }

    @Test
    public void signupTestSuccessful() throws Exception { //test of API: "/api/auth/signup"
        when(this.userRepo.findByEmailIgnoreCase(this.tempUser.getEmail())).thenReturn(null);
        when(this.userRepo.findAll()).thenReturn(new ArrayList<>());
        this.tempUser.setRoles(List.of("ADMINISTRATOR"));
        when(this.userRepo.save(any(User.class))).thenReturn(this.tempUser);


        Map<String, Object> userMap = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(this.tempUser), Map.class);
        userMap.put("password", this.tempUser.getPassword());
        userMap.remove("id");
        userMap.remove("roles");

        String json = JSONValue.toJSONString(userMap);

        this.mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isOk());

        verify(this.userRepo, times(1)).findByEmailIgnoreCase(anyString());
        verify(this.userRepo, times(1)).findAll();
        verify(this.userRepo, times(1)).save(any(User.class));
    }

    @Test
    public void changePassTestSuccessful() throws Exception { //test of API: "/api/auth/changepass"
        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("new_password", "aynuraNewPassword");
        String json = JSONValue.toJSONString(passwordRequest);

        when(this.userRepo.updatePassword(anyString(), anyString())).thenReturn(1);

        this.mockMvc.perform(post("/api/auth/changepass").with(user(new UserDetailsImpl(this.tempUser))) // we used with() method for authentication of a user. Note that this authentication is mocked (not confirmed with database)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isOk());
    }

    @Test
    public void changePassUnsuccessful() throws Exception { //test of API: "/api/auth/changepass"
        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("new_password", "aynuraNewPassword");
        String json = JSONValue.toJSONString(passwordRequest);

        this.mockMvc.perform(post("/api/auth/changepass") // we didn't use with() method for authentication of a user!
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isUnauthorized());
    }
}
