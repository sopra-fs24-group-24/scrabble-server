package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    testUser = new User();
    testUser.setId(1L);
    testUser.setName("testName");
    testUser.setUsername("testUsername");

    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
  }

  @Test
  public void createUser_validInputs_success() {
    // when -> any object is being save in the userRepository -> return the dummy
    // testUser
    User createdUser = userService.createUser(testUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(testUser.getId(), createdUser.getId());
    assertEquals(testUser.getName(), createdUser.getName());
    assertEquals(testUser.getUsername(), createdUser.getUsername());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
  }

  @Test
  public void createUser_duplicateName_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
    // Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }

  @Test
  public void createUser_duplicateInputs_throwsException() {
    // given -> a first user has already been created
    userService.createUser(testUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByName(Mockito.any())).thenReturn(testUser);
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
  }

  @Test
  public void updateUser_invalidId_throwsException() {
    userService.createUser(testUser);

    User updatedUser = new User();
    updatedUser.setUsername("newUsername");

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> userService.updateUser(updatedUser, 2L));
  }

  @Test
  public void updateUser_invalidToken_throwsException() {
    userService.createUser(testUser);

    User updatedUser = new User();
    updatedUser.setUsername("newUsername");
    updatedUser.setToken("invalidToken");

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));

    assertThrows(ResponseStatusException.class, () -> userService.updateUser(updatedUser, 1L));
  }

  @Test
  public void updateUser_emptyUsername_throwsException() {
    User createdUser = userService.createUser(testUser);

    User updatedUser = new User();
    updatedUser.setUsername("");
    updatedUser.setToken(createdUser.getToken());

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));

    assertThrows(ResponseStatusException.class, () -> userService.updateUser(updatedUser, 1L));
  }

  @Test
  public void updateUser_duplicateUsername_throwsException() {
    User createdUser = userService.createUser(testUser);

    User updatedUser = new User();
    updatedUser.setUsername("newUsername");
    updatedUser.setToken(createdUser.getToken());

    User secondUser = new User();
    secondUser.setUsername("newUsername");
    secondUser.setId(2L);

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(secondUser);

    assertThrows(ResponseStatusException.class, () -> userService.updateUser(updatedUser, 1L));
  }

  @Test
  public void updateUser_validInput_success() {
    User createdUser = userService.createUser(testUser);

    User updatedUser = new User();
    updatedUser.setUsername("newUsername");
    updatedUser.setToken(createdUser.getToken());

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

    userService.updateUser(updatedUser, 1l);

    assertEquals(updatedUser.getUsername(), createdUser.getUsername());
  }

  @Test
  public void logoutUser_validInput_success() {
    User createdUser = userService.createUser(testUser);

    User user = new User();
    user.setId(1L);
    user.setToken(createdUser.getToken());

    Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(testUser));

    userService.logoutUser(user);

    assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
  }
}
