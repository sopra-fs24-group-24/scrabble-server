package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LogoutPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void givenUsers_whenGetWithPattern_thenReturnJsonArray() throws Exception {
      User user = new User();
      user.setUsername("testUsername");
      user.setStatus(UserStatus.OFFLINE);

      List<User> foundUsers = Collections.singletonList(user);

      given(userService.searchUser(Mockito.any())).willReturn(foundUsers);

      MockHttpServletRequestBuilder getRequest = get("/users/?pattern=test").contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(getRequest).andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(1)))
              .andExpect(jsonPath("$[0].username", is(user.getUsername())))
              .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }

  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  
  @Test
  public void createUser_invalidInput_userNotCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setToken("1");

    // given(userService.createUser(Mockito.any())).willReturn(user);

    given(userService.createUser(user)).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated());
        //.andExpect(jsonPath("$.id", is(user.getId().intValue())))
        //.andExpect(jsonPath("$.name", is(user.getName())))
        //.andExpect(jsonPath("$.username", is(user.getUsername())))
        //.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    
        ResponseStatusException e=new ResponseStatusException(HttpStatus.CONFLICT);

        given(userService.createUser(Mockito.any())).willThrow(e);
        //given(userService.checkIfUserExists(user)).willReturn(Exception);
    // then
    mockMvc.perform(postRequest)
    .andExpect(status().isConflict());
  }

  @Test
  public void updateUser_validInput_userUpdated() throws Exception {
      UserPutDTO userPutDTO = new UserPutDTO();
      userPutDTO.setToken("testToken");
      userPutDTO.setUsername("testUsername");

      MockHttpServletRequestBuilder putRequest = put("/users/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPutDTO));

      mockMvc.perform(putRequest)
              .andExpect(status().isNoContent());
  }

  @Test
  public void updateUser_invalidInput_userNotUpdated() throws Exception {
      UserPutDTO userPutDTO = new UserPutDTO();
      userPutDTO.setToken("testToken");
      userPutDTO.setUsername("testUsername");

      ResponseStatusException response = new ResponseStatusException(HttpStatus.NOT_FOUND, "The user with id: 1 doesn't exist!");

      doThrow(response).when(userService).updateUser(isA(User.class), eq(1L));

      MockHttpServletRequestBuilder putRequest = put("/users/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPutDTO));

      mockMvc.perform(putRequest)
              .andExpect(status().isNotFound())
              .andExpect(status().reason("The user with id: 1 doesn't exist!"));
  }
  
  @Test
  public void loginUser_validInput() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("1234");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setPassword("1234");
    userPostDTO.setUsername("testUsername");

    given(userService.attemptUserLogin(user.getUsername(),user.getPassword())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/logins")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  @Test
  public void loginUser_invalidInput() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setPassword("1234");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setPassword("1234");
    userPostDTO.setUsername("testUsername");
    userPostDTO.setToken("1");

    // given(userService.createUser(Mockito.any())).willReturn(user);
    
    ResponseStatusException e=new ResponseStatusException(HttpStatus.FORBIDDEN);

    given(userService.attemptUserLogin(user.getUsername(),user.getPassword())).willThrow(e);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/logins")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isForbidden());
        //.andExpect(jsonPath("$.id", is(user.getId().intValue())))
        //.andExpect(jsonPath("$.name", is(user.getName())))
        //.andExpect(jsonPath("$.username", is(user.getUsername())))
        //.andExpect(jsonPath("$.status", is(user.getStatus().toString())));

  }

  @Test
  public void logoutUser_validInput() throws Exception {
    LogoutPostDTO logoutPostDTO = new LogoutPostDTO();
    logoutPostDTO.setToken("testToken");
    logoutPostDTO.setId(1L);

    MockHttpServletRequestBuilder postRequest = post("/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(logoutPostDTO));

    mockMvc.perform(postRequest)
            .andExpect(status().isNoContent());
  }

  @Test
  public void logoutUser_invalidInput() throws Exception {
      LogoutPostDTO logoutPostDTO = new LogoutPostDTO();
      logoutPostDTO.setToken("testToken");
      logoutPostDTO.setId(1L);

      ResponseStatusException response = new ResponseStatusException(HttpStatus.NOT_FOUND, "The user with id: 1 doesn't exist!");

      doThrow(response).when(userService).logoutUser(isA(User.class));

      MockHttpServletRequestBuilder postRequest = post("/logout")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(logoutPostDTO));

      mockMvc.perform(postRequest)
              .andExpect(status().isNotFound());
  }

  @Test
  public void getUser_validInputs() throws Exception 
  {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));

// when/then -> do the request + validate the result
MockHttpServletRequestBuilder getRequest = get("/users/1")
.contentType(MediaType.APPLICATION_JSON)
.content(asJsonString(userPostDTO));

given(userService.isTokenValid(Mockito.any())).willReturn(user);
given(userService.getUserParams(Mockito.any())).willReturn(user);

// then
mockMvc.perform(getRequest)
.andExpect(status().isOk())
.andExpect(jsonPath("$.id", is(user.getId().intValue())))
.andExpect(jsonPath("$.name", is(user.getName())))
.andExpect(jsonPath("$.username", is(user.getUsername())))
.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  @Test
  public void getUser_nonExistant() throws Exception 
  {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));

// when/then -> do the request + validate the result
MockHttpServletRequestBuilder getRequest = get("/users/2")
.contentType(MediaType.APPLICATION_JSON)
.content(asJsonString(userPostDTO));

ResponseStatusException e=new ResponseStatusException(HttpStatus.NOT_FOUND);

given(userService.isTokenValid(Mockito.any())).willReturn(user);
given(userService.getUserParams(Long.parseLong("2"))).willThrow(e);

// then
mockMvc.perform(getRequest)
.andExpect(status().isNotFound());
  }

  @Test
  public void addFriend_validInput_friendAdded() throws Exception {
      MockHttpServletRequestBuilder putRequest = put("/friends/1/2/?token=testToken")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(putRequest)
              .andExpect(status().isNoContent());
  }

  @Test
  public void addFriend_invalidFriendId_noAddedFriend() throws Exception {
      ResponseStatusException response = new ResponseStatusException(HttpStatus.NOT_FOUND);
      doThrow(response).when(userService).addFriend(Mockito.any(), Mockito.any(), Mockito.any());

      MockHttpServletRequestBuilder putRequest = put("/friends/1/2/?token=testToken")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(putRequest)
              .andExpect(status().isNotFound());
  }

  @Test
  public void addFriend_alreadyHasFriendRequest_noAddedFriend() throws Exception {
      ResponseStatusException response = new ResponseStatusException(HttpStatus.CONFLICT);
      doThrow(response).when(userService).addFriend(Mockito.any(), Mockito.any(), Mockito.any());

      MockHttpServletRequestBuilder putRequest = put("/friends/1/2/?token=testToken")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(putRequest)
              .andExpect(status().isConflict());
  }

  @Test
  public void removeFriend_validInput_friendRemoved() throws Exception {
      MockHttpServletRequestBuilder deleteRequest = delete("/friends/1/2/?token=testToken")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(deleteRequest)
              .andExpect(status().isNoContent());
  }

  @Test
  public void removeFriend_invalidFriendId_nothingRemoved() throws Exception {
      ResponseStatusException response = new ResponseStatusException(HttpStatus.NOT_FOUND);
      doThrow(response).when(userService).removeFriend(Mockito.any(), Mockito.any(), Mockito.any());

      MockHttpServletRequestBuilder deleteRequest = delete("/friends/1/2/?accept=true&token=testToken")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(deleteRequest)
              .andExpect(status().isNotFound());
  }

  @Test
  public void acceptFriendRequest_validInput_friendAdded() throws Exception {
      MockHttpServletRequestBuilder postRequest = post("/friends/1/2/?accept=true&token=testToken")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(postRequest)
              .andExpect(status().isNoContent());
  }

  @Test
  public void acceptFriendRequest_invalidFriendId_nothingAdded() throws Exception {
      ResponseStatusException response = new ResponseStatusException(HttpStatus.NOT_FOUND);
      doThrow(response).when(userService).acceptFriendRequest(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

      MockHttpServletRequestBuilder postRequest = post("/friends/1/2/?accept=true&token=testToken")
              .contentType(MediaType.APPLICATION_JSON);

      mockMvc.perform(postRequest)
              .andExpect(status().isNotFound());
  }

  @Test
  public void getFriends_validRequest_friendsListSent() throws Exception {
      // given
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("fabio");
      user1.setToken("1");
      User user2 = new User();
      user2.setId(2L);
      user2.setUsername("luca");
      User user3 = new User();
      user3.setId(3L);
      user3.setUsername("martin");

      List<User> friends = new ArrayList<>();
      friends.add(user2);
      friends.add(user3);

      given(userService.getFriends(Mockito.any(), Mockito.any())).willReturn(friends);

      // when
      MockHttpServletRequestBuilder getRequest = get("/friends/1?token=1");

      // then
      mockMvc.perform(getRequest).andExpect(status().isOk())
              .andExpect(jsonPath("$", hasSize(2)))
              .andExpect(jsonPath("$[0].id", anyOf(is(user2.getId().intValue()), is(user3.getId().intValue()))))
              .andExpect(jsonPath("$[0].username", anyOf(is(user2.getUsername()), is(user3.getUsername()))))
              .andExpect(jsonPath("$[1].id", anyOf(is(user2.getId().intValue()), is(user3.getId().intValue()))))
              .andExpect(jsonPath("$[1].username", anyOf(is(user2.getUsername()), is(user3.getUsername()))))
              .andExpect(jsonPath("$[0].password", is("")))
              .andExpect(jsonPath("$[1].password", is("")))
              .andExpect(jsonPath("$[0].token", is("")))
              .andExpect(jsonPath("$[1].token", is("")));
  }

  @Test
  public void getInformationFromOtherUser_validRequest_userInformationSent() throws Exception {
      // given
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("fabio");
      user1.setToken("1");
      User user2 = new User();
      user2.setId(2L);
      user2.setUsername("luca");

      given(userService.isTokenValid(Mockito.any())).willReturn(user1);
      given(userService.getUserParams(Mockito.any())).willReturn(user2);

      // when
      MockHttpServletRequestBuilder getRequest = get("/users/2?token=1");

      // then
      mockMvc.perform(getRequest).andExpect(status().isOk())
              .andExpect(jsonPath("$.id", is(user2.getId().intValue())))
              .andExpect(jsonPath("$.username", is(user2.getUsername())))
              .andExpect(jsonPath("$.password", is("")))
              .andExpect(jsonPath("$.token", is("")));
  }

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}