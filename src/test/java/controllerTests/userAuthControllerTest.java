package controllerTests;

import account.AccountServiceApplication;
import account.Domain.User;
import account.Repositories.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {AccountServiceApplication.class})
@AutoConfigureMockMvc
public class userAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepo userRepo;

    @Spy
    private User user;

    @BeforeEach
    public void createData() {
        this.user = User.builder().name("Aynura").lastname("Rejepova")
                .email("aynura@acme.com").password("dolceGabana77").build();
    }

    @Test
    public void signupTestUnsuccessful() throws Exception {
        when(this.userRepo.findByEmailIgnoreCase(anyString())).thenReturn(this.user);

        this.mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(this.user))).andExpect(status().isBadRequest());
    }

    @Test
    public void signupTestSuccessful() throws Exception {
        when(this.userRepo.findByEmailIgnoreCase(this.user.getEmail())).thenReturn(null);
        when(this.userRepo.findAll()).thenReturn(new ArrayList<>());
        this.user.setRoles(List.of("ADMINISTRATOR"));
        when(this.userRepo.save(any(User.class))).thenReturn(this.user);


        Map<String, Object> userMap = new ObjectMapper().readValue(new ObjectMapper().writeValueAsString(this.user), Map.class);
        userMap.put("password", this.user.getPassword());
        userMap.remove("id");
        userMap.remove("roles");

        String json = JSONValue.toJSONString(userMap);

        this.mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isOk());

        verify(this.userRepo, times(1)).findByEmailIgnoreCase(anyString());
        verify(this.userRepo, times(1)).findAll();
        verify(this.userRepo, times(1)).save(any(User.class));
    }
}
