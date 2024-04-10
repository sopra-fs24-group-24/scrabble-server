package ch.uzh.ifi.hase.soprafs24.rest.dto;

public class UserPutDTO {
    private String username;
    private String token;

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
