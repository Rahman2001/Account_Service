package repoTests;

import account.AccountServiceApplication;
import account.Domain.User;
import account.Repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {AccountServiceApplication.class})
@EnableAutoConfiguration
public class userRepoTest {

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    public void insertTestData() {
        User user = User.builder().id(1L).email("rahym@acme.com").name("Rahman")
                .lastname("Rejepov").password("dolceGabana").roles(List.of("USER")).build();
        this.userRepo.save(user);
    }
    @Test
    public void findEmailIgnoreCaseTest() {
        assertNotNull(this.userRepo.findByEmailIgnoreCase("rahym@acme.com"));
    }

    @Test
    public void updatePasswordTest() {
        assertEquals(1, this.userRepo.updatePassword("rahym@acme.com", "dolceGabana77"));
    }

    @Test
    public void getRolesOfUserTest() {
        assertNotNull(this.userRepo.getRolesOfUser("rahym@acme.com"));
    }

    @Test
    public void hasAnyRoleTest() {
        assertTrue(this.userRepo.hasAnyRoles("rahym@acme.com"));
    }

    @Test
    public void getAllUserSortedById() {
        assertNotNull(this.userRepo.getAllUsersSortedById());
        List<User> userList = this.userRepo.getAllUsersSortedById();
        if(userList.size() > 1) {
            assertTrue(userList.get(0).getId() < userList.get(1).getId());
        }
    }

    @Test
    public void deleteUserTest() {
        assertEquals(1, this.userRepo.deleteUser("rahym@acme.com"));
    }
}
