package account.Repositories;

import account.Domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRepo extends CrudRepository<User, String> {
    User findByEmailIgnoreCase(String email);

    @Modifying(clearAutomatically = true)// This annotation is needed in id to be able to update the table, otherwise @Query won't do this operation
    @Query("UPDATE User u SET u.password = ?2 WHERE u.email = ?1")
    @Transactional //since we use update/delete operation, we have to use transactional state of it (in case, it won't succeed)
    Integer updatePassword(String email, String newPassword);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.roles = ?2 WHERE u.email = ?1")
    @Transactional
    Integer updateUserRole(String email, String[] roles);

    @Query(value = "SELECT roles FROM User WHERE email = ?1", nativeQuery = true)
    String getRolesOfUser(String email);

    @Query(value = "SELECT roles IS NOT NULL FROM User WHERE email = ?1", nativeQuery = true)
    boolean hasAnyRoles(String email);

    @Query(value = "SELECT * FROM User ORDER BY id DESC", nativeQuery = true)
    List<User> getAllUsersSortedById();

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM User user WHERE user.email = ?1")
    @Transactional
    Integer deleteUser(String email);

}