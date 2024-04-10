package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers(@RequestParam(required=false) String token) 
  {
    userService.isTokenValid(token);

    // fetch all users in the internal representation
    List<User> users = userService.getUsers();
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) 
    {
        // strip critical information
        user.setPassword("");
        user.setToken("");
        userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @PutMapping("/users/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateUser(@PathVariable("userId") Long id, @RequestBody UserPutDTO userPutDTO) {
    User userInput = DTOMapper.INSTANCE.convertUserPutDTOToEntity(userPutDTO);
    userService.updateUser(userInput, id);
  }

  @RequestMapping("/users/{userId}")
  @ResponseBody
  public User getUser(@PathVariable("userId") Long userId,@RequestParam(required=false) String token) 
  {
    // Who is asking?
    User tokenHolder=userService.isTokenValid(token);

    // Are they asking for their own information?
    if(tokenHolder.getId()==userId)
    {
        return userService.getUserParams(userId);
    }
    else // strip critical data:
    {
        User requestedUser=userService.getUserParams(userId);
        requestedUser.setPassword("");
        requestedUser.setToken("");
        return requestedUser;
    }
        
  }

  @PostMapping("/logins")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    User loggedinUser=userService.attemptUserLogin(userInput.getUsername(), userInput.getPassword());

    // convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedinUser);
  }

  @GetMapping("/friends/{userId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getFriends(@PathVariable("userId") Long userId, @RequestParam String token) {
    List<User> friends = userService.getFriends(userId, token);
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    for (User user : friends) {
        user.setPassword("");
        user.setToken("");
        userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }

    return userGetDTOs;
  }
}
