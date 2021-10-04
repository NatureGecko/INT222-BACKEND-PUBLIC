package swst.application.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import swst.application.entities.Roles;
import swst.application.entities.UsernamesModels;

@Repository
public interface UsernameRepository extends JpaRepository<UsernamesModels, Integer> {

	UsernamesModels findByUserName(String userName);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByUserNameIgnoreCase(String userName);

	boolean existsByPhoneNumber(String phoneNumber);

}
