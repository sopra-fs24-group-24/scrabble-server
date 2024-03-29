package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
  User findByName(String name);

  User findByUsername(String username);

  User findByToken(String token);




}
