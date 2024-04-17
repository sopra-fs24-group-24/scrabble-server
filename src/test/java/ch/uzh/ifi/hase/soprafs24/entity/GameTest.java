package ch.uzh.ifi.hase.soprafs24.entity;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
}
