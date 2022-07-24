package repoTests;

import account.AccountServiceApplication;
import account.Domain.PaymentsToEmployee;
import account.Domain.User;
import account.Repositories.PaymentRepo;
import account.Repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
@ExtendWith(SpringExtension.class)
@DataJpaTest
@ContextConfiguration(classes = {AccountServiceApplication.class})
@EnableAutoConfiguration
public class paymentRepoTest {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private UserRepo userRepo;

    @BeforeEach
    public void insertData() {
        User user = User.builder().id(1L).email("rahym@acme.com").name("Rahym").lastname("Rejepov").build();
        PaymentsToEmployee payments = PaymentsToEmployee.builder().id(1L).user(user).employee(user.getEmail())
                .period("01-2021").salary("123 dollar(s) 23 cent(s)").build();
        this.userRepo.save(user);
        this.paymentRepo.save(payments);
    }

    @Test
    public void findAllByEmailTest() {
        assertEquals(anyList(), this.paymentRepo.findAllByEmail("rahym@acme.com"));
    }

    @Test
    public void findListOfPeriodsOfEmployeeTest() {
        assertEquals(anyList(), this.paymentRepo.findListOfPeriodsOfEmployee("rahym@acme.com"));
    }
}
