package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserPostDTO {

  private String name;

  private String username;

  private String password;

  private String token;

    private ArrayList<Long> friends;

  private UserStatus status;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public ArrayList<Long> getFriends() {
    return friends;
  }

  public void addFriend(Long id) 
  {
    friends.add(id);
  }
}
