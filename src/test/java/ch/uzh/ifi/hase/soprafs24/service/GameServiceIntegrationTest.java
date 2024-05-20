package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;


@WebAppConfiguration
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class GameServiceIntegrationTest {

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();
        userRepository.deleteAll();
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

        userRepository.save(user1);
        userRepository.save(user2);

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
        game.setCurrentPlayer(userRepository.findAll().get(0).getId());
        game.setBag(bag);
        List<User> players = new ArrayList<>();
        players.add(userRepository.findAll().get(0));
        players.add(userRepository.findAll().get(1));
        game.setPlayers(players);
        List<Hand> handsInGame = new ArrayList<>();
        handsInGame.add(hand1);
        handsInGame.add(hand2);
        game.setHands(handsInGame);
        game.setWordContested(false);
        game.setGameRound(2L);

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
        Tile tile5 = new Tile('Z', 10);
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
        score1.setScoreUserId(userRepository.findAll().get(0).getId());
        Score score2 = new Score();
        score2.setScore(0);
        score2.setScoreUserId(userRepository.findAll().get(1).getId());

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);

        Game savedGame = gameRepository.save(game);
        gameRepository.flush();

        // when
        List<Tile> expectedPlayfield = savedGame.getOldPlayfield();
        Long expectedNextPlayer = savedGame.getPlayers().get(1).getId();
        gameService.contestWord(savedGame.getId(), user2, true);

        // then
        Game currentGame = gameRepository.findAll().get(0);

        assertFalse(currentGame.getContestingPhase());
        assertEquals(expectedNextPlayer, currentGame.getCurrentPlayer());
        assertArrayEquals(expectedPlayfield.toArray(), currentGame.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), currentGame.getPlayfield().toArray());
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

        userRepository.save(user1);
        userRepository.save(user2);

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
        hand1.setHanduserid(userRepository.findAll().get(0).getId());

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
        hand2.setHanduserid(userRepository.findAll().get(1).getId());

        game.setDecisionPlayersContestation(new HashMap<Long, Boolean>());
        game.setCurrentPlayer(userRepository.findAll().get(0).getId());
        game.setBag(bag);
        List<User> players = new ArrayList<>();
        players.add(userRepository.findAll().get(0));
        players.add(userRepository.findAll().get(1));
        game.setPlayers(players);
        List<Hand> handsInGame = new ArrayList<>();
        handsInGame.add(hand1);
        handsInGame.add(hand2);
        game.setHands(handsInGame);
        game.setWordContested(false);
        game.setGameRound(2L);

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

        currentPlayfield.set(112, tile6);
        currentPlayfield.set(113, tile7);
        currentPlayfield.set(114, tile8);
        currentPlayfield.set(84, tile4);
        currentPlayfield.set(99, tile5);
        game.setPlayfield(currentPlayfield);

        Score score1 = new Score();
        score1.setScoreUserId(userRepository.findAll().get(0).getId());
        score1.setScore(0);
        Score score2 = new Score();
        score2.setScoreUserId(userRepository.findAll().get(1).getId());
        score2.setScore(10);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);

        Game savedGame = gameRepository.save(game);
        gameRepository.flush();

        // when
        List<Tile> expectedPlayfield = savedGame.getPlayfield();
        Long expectedNextPlayer = userRepository.findAll().get(1).getId();
        gameService.contestWord(savedGame.getId(), user2, false);

        // then
        Game currentGame = gameRepository.findAll().get(0);
        assertEquals(expectedNextPlayer, currentGame.getCurrentPlayer());
        assertFalse(currentGame.getWordContested());
        assertArrayEquals(expectedPlayfield.toArray(), currentGame.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), currentGame.getPlayfield().toArray());
        assertEquals(0, currentGame.getBag().getTiles().size());
    }
}
