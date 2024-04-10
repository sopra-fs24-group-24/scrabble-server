package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);

  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);

    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  public void updateUser(User user, Long id) {
      Optional<User> userById = userRepository.findById(id);
      User userByUsername = userRepository.findByUsername(user.getUsername());

      if (userById.isEmpty()) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("The user with id: %s doesn't exist!", id));
      } else if (!userById.get().getToken().equals(user.getToken())) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User could not be authenticated. Therefore the user could not be updated!");
      }

      if (user.getUsername().isEmpty()) {
          throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "The username is empty. Therefore, the user could not be updated!");
      } else if (userByUsername != null && !userByUsername.getId().equals(id)) {
          throw new ResponseStatusException(HttpStatus.CONFLICT, "The username is not unique. Therefore, the user could not be updated!");
      }

      userById.get().setUsername(user.getUsername());
  }

  public List<User> getFriends(Long id, String token) {
    authenticateUser(id, token);
    List<Long> friendIds = userRepository.findById(id).get().getFriends();
    List<User> friends = new ArrayList<>();

    for (Long friendId : friendIds) {
      friends.add(userRepository.findById(friendId).get());
    }

    return friends;
  }

  private void authenticateUser(Long id, String token) {
    Optional<User> userById = userRepository.findById(id);

    if (userById.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("The user with id: %s doesn't exist!", id));
    } else if (!userById.get().getToken().equals(token)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User could not be authenticated!");
    }
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  public User checkIfUserExists(User userToBeCreated) // TODO: private to public for testing and return type was void
  {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
    
    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) 
    {
     
          throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
    } 
    return userToBeCreated;
  }

  public User isTokenValid(String token)
  {
    User foundUser=userRepository.findByToken(token);
    String baseErrorMessage = "Invalid Token!";
    if (foundUser== null) 
    {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format(baseErrorMessage));
    }
    return foundUser;
}

  public User getUserParams(Long userId)
  {
    Optional<User> user=userRepository.findById(userId);
    if(user.isPresent())
        return user.get();

    String baseErrorMessage = "The %s provided can not be found!";
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format(baseErrorMessage, "User ID"));
  }

  public User attemptUserLogin(String userName, String password)
  {
    User identifiedUser=userRepository.findByUsername(userName);

    if(identifiedUser==null||(!identifiedUser.getPassword().equals(password)))
    {
        String baseErrorMessage = "The entered credentials are invalid!";
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, String.format(baseErrorMessage, "User ID"));
    }

    identifiedUser.setStatus(UserStatus.ONLINE);
    return identifiedUser;

  }
}
