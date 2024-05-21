package ch.uzh.ifi.hase.soprafs24.entity;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GameTest {

    @Test
    public void getNextPlayer_correctOutput() {
        Game game = new Game();

        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);

        List<User> users = new ArrayList<User>();
        users.add(user1);
        users.add(user2);

        game.setPlayers(users);
        game.setCurrentPlayer(2L);

        assertEquals(game.getNextPlayer(), user1);
    }

    @Test
    public void adjustFinalScores_success(){
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(3L);

        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);

        Game game = new Game();
        game.setId(4L);
        game.setPlayers(players);

        List<Tile> handTiles1 = new ArrayList<>();
        for (int i = 0; i < 7; i++){
            handTiles1.add(new Tile('A', 1));
        }
        Hand hand1 = new Hand();
        hand1.setHandtiles(handTiles1);
        hand1.setId(10L);
        hand1.setHanduserid(1L);

        List<Tile> handTiles2 = new ArrayList<>();
        for (int i = 0; i < 2; i++){
            handTiles2.add(new Tile('C', 2));
        }
        Hand hand2 = new Hand();
        hand2.setHandtiles(handTiles2);
        hand2.setId(11L);
        hand2.setHanduserid(2L);

        List<Tile> handTiles3 = new ArrayList<>();
        Hand hand3 = new Hand();
        hand3.setHandtiles(handTiles3);
        hand3.setId(12L);
        hand3.setHanduserid(3L);

        List<Hand> hands = new ArrayList<>();
        hands.add(hand1);
        hands.add(hand2);
        hands.add(hand3);
        game.setHands(hands);

        Score score1 = new Score();
        score1.setScore(2);
        score1.setScoreUserId(1L);
        score1.setId(20L);

        Score score2 = new Score();
        score2.setScore(80);
        score2.setScoreUserId(2L);
        score2.setId(21L);

        Score score3 = new Score();
        score3.setScore(100);
        score3.setScoreUserId(3L);
        score3.setId(22L);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        scores.add(score3);
        game.setScores(scores);

        // when
        Game updatedGame = game.initialiseGameOver();

        // then
        List<Long> playersIDs = new ArrayList<>();
        playersIDs.add(user1.getId());
        playersIDs.add(user2.getId());
        playersIDs.add(user3.getId());
        assertEquals(3, updatedGame.getPlayers().size());
        assertTrue(playersIDs.contains(updatedGame.getPlayers().get(0).getId()));
        assertTrue(playersIDs.contains(updatedGame.getPlayers().get(1).getId()));
        assertTrue(playersIDs.contains(updatedGame.getPlayers().get(2).getId()));

        for (Score score : updatedGame.getScores()){
            if (score.getScoreUserId() == user1.getId()){
                assertEquals(0, score.getScore());
            }
            else if (score.getScoreUserId() == user2.getId()){
                assertEquals(76, score.getScore());
            }
            else{
                assertEquals(111, score.getScore());
            }
        }
    }
}
