package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

public class HighscorePostDTO 
{


  private int score;
  private Long userId;
  private LocalDateTime created;

    public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public Long getUserid() {
    return userId;
  }

  public void setUserid(Long userId) 
  {
    this.userId = userId;
  }

  public LocalDateTime getCreated() 
  {
    return created;
  }

  public void setCreated(LocalDateTime created) 
  {
    this.created = created;
  }

}
