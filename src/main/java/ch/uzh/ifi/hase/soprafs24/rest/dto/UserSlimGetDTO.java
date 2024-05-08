package ch.uzh.ifi.hase.soprafs24.rest.dto;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class UserSlimGetDTO {
    private Long id;
    private String username;
    private UserStatus status;

    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }

    public void setUsername(String username) { this.username = username; }

    public String getUsername() { return username; }

    public void setStatus(UserStatus status) { this.status = status; }

    public UserStatus getStatus() { return status; }
}
