package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long id;

  @Column(nullable = true)
  private String name;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(nullable = false)
  private UserStatus status;

  @Column(nullable = false)
  private String password;

  @Column(nullable = true)
  @ElementCollection
  private List<Long> friends = new ArrayList<Long>();

  @Column(nullable = false)
  @ElementCollection
  private List<Long> friendRequests = new ArrayList<>();

  @Column(nullable = false)
  @ElementCollection
  private List<Long> sentFriendRequests = new ArrayList<>();

    public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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

  public String getPassword() 
  {
    return password;
  }

  public void setPassword(String password) 
  {
    this.password = password;
  }

  public List<Long> getFriends()
  {
    return friends;
  }

  public void addFriend(Long friend)
  {
    friends.add(friend);
  }

  public void removeFriend(Long friend) { friends.remove(friend); }

  public List<Long> getFriendRequests() { return friendRequests; }

  public void setFriendRequests(List<Long> friendRequests) { this.friendRequests = friendRequests; }

  public void addFriendRequest(Long friend) { friendRequests.add(friend); }

  public void removeFriendRequest(Long friend) { friendRequests.remove(friend); }

  public List<Long> getSentFriendRequests() { return sentFriendRequests; }

  public void setSentFriendRequests(List<Long> sentFriendRequests) { this.sentFriendRequests = sentFriendRequests; }

  public void addSentFriendRequest(Long friend) { sentFriendRequests.add(friend); }

  public void removeSentFriendRequest(Long friend) { sentFriendRequests.remove(friend); }
}
