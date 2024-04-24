package ch.uzh.ifi.hase.soprafs24.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="Score")
public class Score implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable=false)
    private int Score;

    @Column(nullable = false, unique = true)
    private Long ScoreUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getScore() {
        return Score;
    }

    public void setScore(int score) {
        Score = score;
    }

    public Long getScoreUserId() {
        return ScoreUserId;
    }

    public void setScoreUserId(Long scoreUserId) {
        ScoreUserId = scoreUserId;
    }
}
