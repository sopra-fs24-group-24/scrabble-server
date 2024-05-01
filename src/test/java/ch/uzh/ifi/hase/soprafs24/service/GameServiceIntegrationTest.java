package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;


@WebAppConfiguration
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class GameServiceIntegrationTest {

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;


    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();
    }

    @Test
    public void contestWord_validContest_allPlayersContested_contestSuccessful() {
        // given
        User user1 = new User();
        user1.setUsername("fabio");
        user1.setPassword("1");
        user1.setStatus(UserStatus.ONLINE);
        user1.setToken("1");
        User user2 = new User();
        user2.setPassword("2");
        user2.setUsername("luca");
        user2.setStatus(UserStatus.ONLINE);
        user2.setToken("2");

        Game game = new Game();
        Bag bag = new Bag();

        List<Tile> tilesInBag = new ArrayList<>();
        tilesInBag.add(new Tile('A', 2));
        tilesInBag.add(new Tile('C', 3));
        bag.setTiles(tilesInBag);

        Hand hand1 = new Hand();
        List<Tile> tilesInHand1 = new ArrayList<>();
        tilesInHand1.add(new Tile('A', 2));
        tilesInHand1.add(new Tile('C', 3));
        tilesInHand1.add(new Tile('A', 2));
        tilesInHand1.add(new Tile('Z', 10));
        tilesInHand1.add(new Tile('K', 4));
        tilesInHand1.add(new Tile('T', 5));
        tilesInHand1.add(new Tile('Q', 10));

        Hand hand2 = new Hand();
        List<Tile> tilesInHand2 = new ArrayList<>();
        tilesInHand2.add(new Tile('A', 2));
        tilesInHand2.add(new Tile('C', 3));
        tilesInHand2.add(new Tile('A', 2));
        tilesInHand2.add(new Tile('Z', 10));
        tilesInHand2.add(new Tile('K', 4));
        tilesInHand2.add(new Tile('T', 5));
        tilesInHand2.add(new Tile('Q', 10));

        game.setDecisionPlayersContestation(new HashMap<Long, Boolean>());
        game.setCurrentPlayer(1L);
        game.setBag(bag);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        game.setPlayers(players);
        List<Hand> handsInGame = new ArrayList<>();
        handsInGame.add(hand1);
        handsInGame.add(hand2);
        game.setHands(handsInGame);
        game.setWordContested(true);

        List<Tile> oldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            oldPlayfield.add(null);
        }
        Tile tile1 = new Tile('C', 3);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('A', 2);
        tile2.setBoardidx(113);
        Tile tile3 = new Tile('R', 5);
        tile3.setBoardidx(114);
        oldPlayfield.set(112, tile1);
        oldPlayfield.set(113, tile2);
        oldPlayfield.set(114, tile3);
        game.setOldPlayfield(oldPlayfield);

        List<Tile> currentPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            currentPlayfield.add(null);
        }
        Tile tile4 = new Tile('Q', 10);
        tile4.setBoardidx(84);
        Tile tile5 = new Tile('O', 6);
        tile5.setBoardidx(99);
        Tile tile6 = new Tile('C', 3);
        tile6.setBoardidx(112);
        Tile tile7 = new Tile('A', 2);
        tile7.setBoardidx(113);
        Tile tile8 = new Tile('R', 5);
        tile8.setBoardidx(114);

        currentPlayfield.set(84, tile4);
        currentPlayfield.set(99, tile5);
        currentPlayfield.set(112, tile6);
        currentPlayfield.set(113, tile7);
        currentPlayfield.set(114, tile8);
        game.setPlayfield(currentPlayfield);

        Score score1 = new Score();
        score1.setScore(0);
        score1.setScoreUserId(1L);
        Score score2 = new Score();
        score2.setScore(0);
        score2.setScoreUserId(11L);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);

        Game savedGame = gameRepository.save(game);
        gameRepository.flush();

        // when
        gameService.contestWord(savedGame.getId(), user2, true);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedPlayfield.add(null);
        }

        Tile tile9 = new Tile('C', 3);
        tile9.setBoardidx(112);
        Tile tile10 = new Tile('A', 2);
        tile10.setBoardidx(113);
        Tile tile11 = new Tile('R', 5);
        tile11.setBoardidx(114);
        expectedPlayfield.set(112, tile9);
        expectedPlayfield.set(113, tile10);
        expectedPlayfield.set(114, tile11);

        assertEquals(2L, game.getCurrentPlayer());
        assertFalse(game.getWordContested());
        assertArrayEquals(expectedPlayfield.toArray(), game.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), game.getPlayfield().toArray());
    }

    @Test
    public void contestWord_wordNotContested_oldPlayfieldOverwritten() {
        // given
        User user1 = new User();
        user1.setUsername("fabio");
        user1.setPassword("1");
        user1.setStatus(UserStatus.ONLINE);
        user1.setToken("1");
        User user2 = new User();
        user2.setPassword("2");
        user2.setUsername("luca");
        user2.setStatus(UserStatus.ONLINE);
        user2.setToken("2");

        Game game = new Game();

        Bag bag = new Bag();

        List<Tile> tilesInBag = new ArrayList<>();
        tilesInBag.add(new Tile('A', 2));
        tilesInBag.add(new Tile('C', 3));
        bag.setTiles(tilesInBag);

        Hand hand1 = new Hand();
        List<Tile> tilesInHand1 = new ArrayList<>();
        tilesInHand1.add(new Tile('Q', 10));
        tilesInHand1.add(new Tile('O', 6));
        hand1.setHandtiles(tilesInHand1);
        hand1.setHanduserid(1L);

        Hand hand2 = new Hand();
        List<Tile> tilesInHand2 = new ArrayList<>();
        tilesInHand2.add(new Tile('A', 2));
        tilesInHand2.add(new Tile('C', 3));
        tilesInHand2.add(new Tile('S', 5));
        tilesInHand2.add(new Tile('Z', 10));
        tilesInHand2.add(new Tile('L', 5));
        tilesInHand2.add(new Tile('T', 5));
        tilesInHand2.add(new Tile('Q', 10));
        hand2.setHandtiles(tilesInHand2);
        hand2.setHanduserid(2L);

        game.setDecisionPlayersContestation(new HashMap<Long, Boolean>());
        game.setCurrentPlayer(1L);
        game.setBag(bag);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        game.setPlayers(players);
        List<Hand> handsInGame = new ArrayList<>();
        handsInGame.add(hand1);
        handsInGame.add(hand2);
        game.setHands(handsInGame);
        game.setWordContested(true);

        List<Tile> oldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            oldPlayfield.add(null);
        }
        Tile tile1 = new Tile('C', 3);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('A', 2);
        tile2.setBoardidx(113);
        Tile tile3 = new Tile('R', 5);
        tile3.setBoardidx(114);
        oldPlayfield.set(112, tile1);
        oldPlayfield.set(113, tile2);
        oldPlayfield.set(114, tile3);
        game.setOldPlayfield(oldPlayfield);

        List<Tile> currentPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            currentPlayfield.add(null);
        }
        Tile tile4 = new Tile('Q', 10);
        tile4.setBoardidx(84);
        Tile tile5 = new Tile('O', 6);
        tile5.setBoardidx(99);
        Tile tile6 = new Tile('C', 3);
        tile6.setBoardidx(112);
        Tile tile7 = new Tile('A', 2);
        tile7.setBoardidx(113);
        Tile tile8 = new Tile('R', 5);
        tile8.setBoardidx(114);

        currentPlayfield.set(84, tile4);
        currentPlayfield.set(99, tile5);
        currentPlayfield.set(112, tile6);
        currentPlayfield.set(113, tile7);
        currentPlayfield.set(114, tile8);
        game.setPlayfield(currentPlayfield);

        Score score1 = new Score();
        score1.setScoreUserId(1L);
        score1.setScore(0);
        Score score2 = new Score();
        score2.setScoreUserId(2L);
        score2.setScore(10);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);

        Game savedGame = gameRepository.save(game);
        gameRepository.flush();

        // when
        gameService.contestWord(savedGame.getId(), user2, false);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedPlayfield.add(null);
        }

        Tile tile9 = new Tile('C', 3);
        tile9.setBoardidx(112);
        Tile tile10 = new Tile('A', 2);
        tile10.setBoardidx(113);
        Tile tile11 = new Tile('R', 5);
        tile11.setBoardidx(114);
        Tile tile12 = new Tile('Q', 10);
        tile12.setBoardidx(84);
        Tile tile13 = new Tile('O', 6);
        tile13.setBoardidx(99);
        expectedPlayfield.set(112, tile9);
        expectedPlayfield.set(113, tile10);
        expectedPlayfield.set(114, tile11);
        expectedPlayfield.set(84, tile12);
        expectedPlayfield.set(99, tile13);

        List<Character> lettersInHand1 = new ArrayList<>();
        for (int i = 0; i < 2; i++){
            lettersInHand1.add(game.getHands().get(0).getHandtiles().get(i).getLetter());
        }

        assertEquals(2L, game.getCurrentPlayer());
        assertFalse(game.getWordContested());
        assertArrayEquals(expectedPlayfield.toArray(), game.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), game.getPlayfield().toArray());
        assertEquals(0, game.getBag().getTiles().size());
        assertEquals(2, game.getHands().get(0).getHandtiles().size());
        assertEquals(7, game.getHands().get(1).getHandtiles().size());
        assertTrue(lettersInHand1.contains('A'));
        assertTrue(lettersInHand1.contains('C'));
    }
}
