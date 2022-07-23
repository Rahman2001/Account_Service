package account.Repositories;

import account.Domain.PaymentsToEmployee;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PaymentRepo extends CrudRepository<PaymentsToEmployee, String> {

    @Query(value = "SELECT * FROM PAYMENTS_TO_EMPLOYEE WHERE employee = ?1", nativeQuery = true)
    List<PaymentsToEmployee> findAllByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE PAYMENTS_TO_EMPLOYEE SET salary =?3 WHERE employee = ?1 AND period = ?2", nativeQuery = true)
    int updateEmployeeSalary( String email, String period, String salary);

    @Query(value = "SELECT * FROM PAYMENTS_TO_EMPLOYEE WHERE employee = ?1 ORDER BY period DESC", nativeQuery = true)
    List<PaymentsToEmployee> findListOfPeriodsOfEmployee(String email);

    @Query(value = "SELECT employee FROM PaymentsToEmployee employee WHERE employee.user.email = ?1 AND employee.period = ?2")
    PaymentsToEmployee findPaymentsByEmployeePeriod(String employee, String period);
}
