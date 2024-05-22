package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.Tile;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LobbyServiceIntegrationTest {

    @Qualifier("lobbyRepository")
    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private LobbyService lobbyService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;

    @Qualifier("handRepository")
    @Autowired
    private HandRepository handRepository;

    @Qualifier("scoreRepository")
    @Autowired
    private ScoreRepository scoreRepository;

    @Qualifier("TileRepository")
    @Autowired
    private TileRepository tileRepository;

    @BeforeEach
    public void setup() {
        lobbyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void createLobby_validInputs_success() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        Lobby testLobby = new Lobby();
        List<Long> playersId = new ArrayList<Long>();
        playersId.add(1L);
        testLobby.setUsersInLobby(playersId);
        testLobby.setLobbySize(2);

        // when
        User createdUser = userService.createUser(testUser);
        System.out.println(createdUser.getId());
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // then
        assertEquals(testLobby.getId(), createdLobby.getId());
        assertEquals(testLobby.getUsersInLobby(), createdLobby.getUsersInLobby());
        assertEquals(createdUser.getId(), createdLobby.getPlayers().get(0).getId());
        assertEquals(createdUser.getUsername(), createdLobby.getPlayers().get(0).getUsername());
        assertEquals(createdUser.getToken(), createdLobby.getPlayers().get(0).getToken());
        assertEquals(createdUser.getPassword(), createdLobby.getPlayers().get(0).getPassword());
        assertEquals(testLobby.getLobbySize(), createdLobby.getLobbySize());
        assertEquals(1, createdLobby.getNumberOfPlayers());
        assertFalse(createdLobby.getGameStarted());
        assertNull(createdLobby.getGameOfLobby());
    }

    @Test
    public void createLobby_UserAlreadyInLobby_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        userService.createUser(testUser);
        lobbyService.createLobby(testLobby);

        // attempt to create another Lobby with the same user
        Lobby testLobby2 = new Lobby();
        List<Long> players2 = new ArrayList<Long>();
        players2.add(1L);
        testLobby2.setUsersInLobby(players2);
        testLobby2.setLobbySize(2);

        // check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> lobbyService.createLobby(testLobby2));
    }

    @Test
    public void addPlayerToLobby_validInputs_thenPlayerAddedToLobby() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        User createdUser1 = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser2);
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), 2L);

        // then
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);

        assertEquals(3, updatedLobby.getLobbySize());
        assertEquals(2, updatedLobby.getNumberOfPlayers());
        assertArrayEquals(expectedPlayers.toArray(), updatedLobby.getUsersInLobby().toArray());
        assertFalse(updatedLobby.getGameStarted());
        assertNull(updatedLobby.getGameOfLobby());

        assertEquals(createdUser1.getId(), updatedLobby.getPlayers().get(0).getId());
        assertEquals(createdUser1.getUsername(), updatedLobby.getPlayers().get(0).getUsername());
        assertEquals(createdUser1.getToken(), updatedLobby.getPlayers().get(0).getToken());
        assertEquals(createdUser1.getPassword(), updatedLobby.getPlayers().get(0).getPassword());

        assertEquals(createdUser2.getId(), updatedLobby.getPlayers().get(1).getId());
        assertEquals(createdUser2.getUsername(), updatedLobby.getPlayers().get(1).getUsername());
        assertEquals(createdUser2.getToken(), updatedLobby.getPlayers().get(1).getToken());
        assertEquals(createdUser2.getPassword(), updatedLobby.getPlayers().get(1).getPassword());

    }

    @Test
    public void addPlayerToLobby_validInputs_lobbyFull_thenGameIsStarted() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);

        User createdUser1 = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser2);
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), 2L);

        // then
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);

        assertEquals(2, updatedLobby.getLobbySize());
        assertEquals(2, updatedLobby.getNumberOfPlayers());
        assertArrayEquals(expectedPlayers.toArray(), updatedLobby.getUsersInLobby().toArray());
        assertTrue(updatedLobby.getGameStarted());
        assertNotNull(updatedLobby.getGameOfLobby());

        assertEquals(createdUser1.getId(), updatedLobby.getPlayers().get(0).getId());
        assertEquals(createdUser1.getUsername(), updatedLobby.getPlayers().get(0).getUsername());
        assertEquals(createdUser1.getToken(), updatedLobby.getPlayers().get(0).getToken());
        assertEquals(createdUser1.getPassword(), updatedLobby.getPlayers().get(0).getPassword());

        assertEquals(createdUser2.getId(), updatedLobby.getPlayers().get(1).getId());
        assertEquals(createdUser2.getUsername(), updatedLobby.getPlayers().get(1).getUsername());
        assertEquals(createdUser2.getToken(), updatedLobby.getPlayers().get(1).getToken());
        assertEquals(createdUser2.getPassword(), updatedLobby.getPlayers().get(1).getPassword());
    }


    @Test
    public void addPlayerToLobby_NonExistentLobby_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        userService.createUser(testUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(20L, 1L));
    }

    @Test
    public void addPlayerToLobby_UserAlreadyInLobby_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        Lobby testLobby2 = new Lobby();
        List<Long> players2 = new ArrayList<Long>();
        players2.add(2L);
        testLobby2.setUsersInLobby(players2);
        testLobby2.setLobbySize(4);

        User createdUser = userService.createUser(testUser);
        userService.createUser(testUser2);
        lobbyService.createLobby(testLobby);
        Lobby createdLobby2 = lobbyService.createLobby(testLobby2);

        // when/then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(createdLobby2.getId(), createdUser.getId()));

    }

    @Test
    public void addPlayerToLobby_LobbyAlreadyFull_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        User testUser3 = new User();
        testUser3.setUsername("joel");
        testUser3.setPassword("3");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);

        userService.createUser(testUser);
        User createdUser = userService.createUser(testUser2);
        User createdUser2 = userService.createUser(testUser3);
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), createdUser.getId());

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(updatedLobby.getId(), createdUser2.getId()));

    }

    @Test
    public void deleteLobbyAndGame_success() {
        // check that no lobby or game is saved in database
        assertTrue(gameRepository.findAll().isEmpty());
        assertTrue(lobbyRepository.findAll().isEmpty());
        assertTrue(handRepository.findAll().isEmpty());
        assertTrue(scoreRepository.findAll().isEmpty());
        assertTrue(tileRepository.findAll().isEmpty());
        assertTrue(userRepository.findAll().isEmpty());

        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        User createdUser = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser2);

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(createdUser.getId());
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);
        Lobby createdLobby = lobbyService.createLobby(testLobby);
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), createdUser2.getId());
        Game game = updatedLobby.getGameOfLobby();

        // check that created lobby and game are saved in database
        assertEquals(1, lobbyRepository.findAll().size());
        assertNotNull(lobbyRepository.findAll().get(0).getGameOfLobby());
        assertEquals(1, gameRepository.findAll().size());
        assertEquals(game.getId(), gameRepository.findAll().get(0).getId());
        assertEquals(100, tileRepository.findAll().size());
        assertEquals(2, handRepository.findAll().size());
        if (handRepository.findAll().get(0).getHanduserid() == game.getPlayers().get(0).getId()){
            assertEquals(game.getPlayers().get(0).getId(), handRepository.findAll().get(0).getHanduserid());
            assertEquals(game.getPlayers().get(1).getId(), handRepository.findAll().get(1).getHanduserid());
        }
        else{
            assertEquals(game.getPlayers().get(1).getId(), handRepository.findAll().get(0).getHanduserid());
            assertEquals(game.getPlayers().get(0).getId(), handRepository.findAll().get(1).getHanduserid());
        }
        assertEquals(2, scoreRepository.findAll().size());
        if (scoreRepository.findAll().get(0).getScoreUserId() == game.getPlayers().get(0).getId()){
            assertEquals(game.getPlayers().get(0).getId(), scoreRepository.findAll().get(0).getScoreUserId());
            assertEquals(game.getPlayers().get(1).getId(), scoreRepository.findAll().get(1).getScoreUserId());
        }
        else{
            assertEquals(game.getPlayers().get(1).getId(), scoreRepository.findAll().get(0).getScoreUserId());
            assertEquals(game.getPlayers().get(0).getId(), scoreRepository.findAll().get(1).getScoreUserId());
        }

        // One player leaves game - since one player remains in game, the lobby and game should not be deleted
        lobbyService.removePlayerFromLobby(updatedLobby.getId(), createdUser2.getId());

        // check that lobby and game are still saved in database, but Score and Hand Object of leaving player are deleted
        assertEquals(1, lobbyRepository.findAll().size());
        assertNotNull(lobbyRepository.findAll().get(0).getGameOfLobby());
        assertEquals(1, gameRepository.findAll().size());
        assertEquals(game.getId(), gameRepository.findAll().get(0).getId());
        assertEquals(1, handRepository.findAll().size());
        assertEquals(createdUser.getId(), handRepository.findAll().get(0).getHanduserid());
        assertEquals(1, scoreRepository.findAll().size());
        assertEquals(createdUser.getId(), scoreRepository.findAll().get(0).getScoreUserId());
        assertEquals(100, tileRepository.findAll().size());

        // Last player leaves game - lobby and game should be deleted
        lobbyService.removePlayerFromLobby(updatedLobby.getId(), createdUser.getId());

        // check that lobby and game are deleted
        assertTrue(gameRepository.findAll().isEmpty());
        assertTrue(lobbyRepository.findAll().isEmpty());
        assertTrue(handRepository.findAll().isEmpty());
        assertTrue(scoreRepository.findAll().isEmpty());
        assertTrue(tileRepository.findAll().isEmpty());

        // check that users are not deleted
        assertEquals(2, userRepository.findAll().size());
        if (userRepository.findAll().get(0).getId() == createdUser.getId()){
            assertEquals(createdUser.getId(), userRepository.findAll().get(0).getId());
            assertEquals(createdUser2.getId(), userRepository.findAll().get(1).getId());
        }
        else{
            assertEquals(createdUser.getId(), userRepository.findAll().get(1).getId());
            assertEquals(createdUser2.getId(), userRepository.findAll().get(0).getId());
        }
    }

    @Test
    @Transactional
    public void placeTilesAndNotContestWord() {
        // check that no lobby or game is saved in database
        assertTrue(gameRepository.findAll().isEmpty());
        assertTrue(lobbyRepository.findAll().isEmpty());
        assertTrue(handRepository.findAll().isEmpty());
        assertTrue(scoreRepository.findAll().isEmpty());
        assertTrue(tileRepository.findAll().isEmpty());
        assertTrue(userRepository.findAll().isEmpty());

        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        User createdUser = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser2);

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(createdUser.getId());
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);
        Lobby createdLobby = lobbyService.createLobby(testLobby);
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), createdUser2.getId());
        Game game = updatedLobby.getGameOfLobby();

        // check that created lobby and game are saved in database
        assertEquals(1, lobbyRepository.findAll().size());
        assertNotNull(lobbyRepository.findAll().get(0).getGameOfLobby());
        assertEquals(1, gameRepository.findAll().size());
        assertEquals(game.getId(), gameRepository.findAll().get(0).getId());

        // create Game which Player sends
        List<Tile> updatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            updatedPlayfield.add(null);
        }

        List<Tile> oldHand = new ArrayList<>();
        Long currentPlayersID = game.getCurrentPlayer();
        int index = 110;
        for (int i = 0; i < 7; i++){
            if (currentPlayersID == game.getHands().get(0).getHanduserid()){
                Tile placedTile = game.getHands().get(0).getHandtiles().get(i);
                placedTile.setBoardidx(index);
                updatedPlayfield.set(index, placedTile);
                oldHand.add(game.getHands().get(0).getHandtiles().get(i));
            }
            else{
                Tile placedTile = game.getHands().get(1).getHandtiles().get(i);
                placedTile.setBoardidx(index);
                updatedPlayfield.set(index, placedTile);
                oldHand.add(game.getHands().get(1).getHandtiles().get(i));
            }
            index++;
        }
        Game userGame = new Game();
        userGame.setPlayfield(updatedPlayfield);
        userGame.setId(game.getId());


        gameService.placeTilesOnBoard(userGame);

        Game updatedGame = gameRepository.findAll().get(0);

        // word is placed on playfield
        assertEquals(oldHand.get(0), updatedGame.getPlayfield().get(110));
        assertEquals(oldHand.get(1), updatedGame.getPlayfield().get(111));
        assertEquals(oldHand.get(2), updatedGame.getPlayfield().get(112));
        assertEquals(oldHand.get(3), updatedGame.getPlayfield().get(113));
        assertEquals(oldHand.get(4), updatedGame.getPlayfield().get(114));
        assertEquals(oldHand.get(5), updatedGame.getPlayfield().get(115));
        assertEquals(oldHand.get(6), updatedGame.getPlayfield().get(116));
        assertTrue(updatedGame.getOldPlayfield().stream().allMatch(java.util.Objects::isNull));
        assertEquals(86, updatedGame.getBag().getTiles().size());

        if (currentPlayersID == createdUser.getId()){
            gameService.contestWord(updatedGame.getId(), createdUser2, false);
        }
        else{
            gameService.contestWord(updatedGame.getId(), createdUser, false);
        }
        Game savedGame = gameRepository.findAll().get(0);
        assertEquals(oldHand.get(0), savedGame.getPlayfield().get(110));
        assertEquals(oldHand.get(1), savedGame.getPlayfield().get(111));
        assertEquals(oldHand.get(2), savedGame.getPlayfield().get(112));
        assertEquals(oldHand.get(3), savedGame.getPlayfield().get(113));
        assertEquals(oldHand.get(4), savedGame.getPlayfield().get(114));
        assertEquals(oldHand.get(5), savedGame.getPlayfield().get(115));
        assertEquals(oldHand.get(6), savedGame.getPlayfield().get(116));
        assertArrayEquals(savedGame.getPlayfield().toArray(), savedGame.getOldPlayfield().toArray());
        assertEquals(79, savedGame.getBag().getTiles().size());
        // check that player received new tiles
        if (currentPlayersID == savedGame.getHands().get(0).getHanduserid()){
            assertFalse(Arrays.equals(oldHand.toArray(), savedGame.getHands().get(0).getHandtiles().toArray()));
        }
        else{
            assertFalse(Arrays.equals(oldHand.toArray(), savedGame.getHands().get(1).getHandtiles().toArray()));
        }

        lobbyService.removePlayerFromLobby(updatedLobby.getId(), createdUser.getId());
        lobbyService.removePlayerFromLobby(updatedLobby.getId(), createdUser2.getId());

        // check that lobby and game are deleted
        assertTrue(gameRepository.findAll().isEmpty());
        assertTrue(lobbyRepository.findAll().isEmpty());
        assertTrue(handRepository.findAll().isEmpty());
        assertTrue(scoreRepository.findAll().isEmpty());
        assertTrue(tileRepository.findAll().isEmpty());

        // check that users are not deleted
        assertEquals(2, userRepository.findAll().size());
        if (userRepository.findAll().get(0).getId() == createdUser.getId()){
            assertEquals(createdUser.getId(), userRepository.findAll().get(0).getId());
            assertEquals(createdUser2.getId(), userRepository.findAll().get(1).getId());
        }
        else{
            assertEquals(createdUser.getId(), userRepository.findAll().get(1).getId());
            assertEquals(createdUser2.getId(), userRepository.findAll().get(0).getId());
        }
    }

    /*
    @Test
    @Transactional
    public void placeTilesAndContestWord() {
        // check that no lobby or game is saved in database
        assertTrue(gameRepository.findAll().isEmpty());
        assertTrue(lobbyRepository.findAll().isEmpty());
        assertTrue(handRepository.findAll().isEmpty());
        assertTrue(scoreRepository.findAll().isEmpty());
        assertTrue(tileRepository.findAll().isEmpty());
        assertTrue(userRepository.findAll().isEmpty());

        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        User createdUser = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser2);

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(createdUser.getId());
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);
        Lobby createdLobby = lobbyService.createLobby(testLobby);
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), createdUser2.getId());
        Game game = updatedLobby.getGameOfLobby();

        // check that created lobby and game are saved in database
        assertEquals(1, lobbyRepository.findAll().size());
        assertNotNull(lobbyRepository.findAll().get(0).getGameOfLobby());
        assertEquals(1, gameRepository.findAll().size());
        assertEquals(game.getId(), gameRepository.findAll().get(0).getId());

        // create Game which Player sends
        List<Tile> updatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            updatedPlayfield.add(null);
        }

        List<Tile> oldHand = new ArrayList<>();
        Long currentPlayersID = game.getCurrentPlayer();
        int index = 110;
        for (int i = 0; i < 7; i++){
            if (currentPlayersID == game.getHands().get(0).getHanduserid()){
                Tile placedTile = game.getHands().get(0).getHandtiles().get(i);
                placedTile.setBoardidx(index);
                updatedPlayfield.set(index, placedTile);
                oldHand.add(game.getHands().get(0).getHandtiles().get(i));
            }
            else{
                Tile placedTile = game.getHands().get(1).getHandtiles().get(i);
                placedTile.setBoardidx(index);
                updatedPlayfield.set(index, placedTile);
                oldHand.add(game.getHands().get(1).getHandtiles().get(i));
            }
            index++;
        }
        Game userGame = new Game();
        userGame.setPlayfield(updatedPlayfield);
        userGame.setId(game.getId());


        gameService.placeTilesOnBoard(userGame);

        Game updatedGame = gameRepository.findAll().get(0);

        // word is placed on playfield
        assertEquals(oldHand.get(0), updatedGame.getPlayfield().get(110));
        assertEquals(oldHand.get(1), updatedGame.getPlayfield().get(111));
        assertEquals(oldHand.get(2), updatedGame.getPlayfield().get(112));
        assertEquals(oldHand.get(3), updatedGame.getPlayfield().get(113));
        assertEquals(oldHand.get(4), updatedGame.getPlayfield().get(114));
        assertEquals(oldHand.get(5), updatedGame.getPlayfield().get(115));
        assertEquals(oldHand.get(6), updatedGame.getPlayfield().get(116));
        assertTrue(updatedGame.getOldPlayfield().stream().allMatch(java.util.Objects::isNull));
        assertEquals(86, updatedGame.getBag().getTiles().size());

        if (currentPlayersID == createdUser.getId()){
            gameService.contestWord(updatedGame.getId(), createdUser2, true);
        }
        else{
            gameService.contestWord(updatedGame.getId(), createdUser, true);
        }

        Game savedGame = gameRepository.findAll().get(0);

        if (savedGame.getBag().getTiles().size() == 86){
            // Contestation was successful since the size of the bag is the same as before of the function call contestWord()
            assertTrue(savedGame.getOldPlayfield().stream().allMatch(java.util.Objects::isNull));
            assertArrayEquals(savedGame.getOldPlayfield().toArray(), savedGame.getPlayfield().toArray());
            assertEquals(86, savedGame.getBag().getTiles().size());
            // check that player didn't receive new tiles
            if (currentPlayersID == savedGame.getHands().get(0).getHanduserid()){
                assertArrayEquals(oldHand.toArray(), savedGame.getHands().get(0).getHandtiles().toArray());
            }
            else{
                assertArrayEquals(oldHand.toArray(), savedGame.getHands().get(1).getHandtiles().toArray());
            }
        }
        else{
            // Contestation was not successful
            assertEquals(oldHand.get(0), savedGame.getPlayfield().get(110));
            assertEquals(oldHand.get(1), savedGame.getPlayfield().get(111));
            assertEquals(oldHand.get(2), savedGame.getPlayfield().get(112));
            assertEquals(oldHand.get(3), savedGame.getPlayfield().get(113));
            assertEquals(oldHand.get(4), savedGame.getPlayfield().get(114));
            assertEquals(oldHand.get(5), savedGame.getPlayfield().get(115));
            assertEquals(oldHand.get(6), savedGame.getPlayfield().get(116));
            assertArrayEquals(savedGame.getPlayfield().toArray(), savedGame.getOldPlayfield().toArray());
            assertEquals(79, savedGame.getBag().getTiles().size());
            // check that player received new tiles
            if (currentPlayersID == savedGame.getHands().get(0).getHanduserid()){
                assertFalse(Arrays.equals(oldHand.toArray(), savedGame.getHands().get(0).getHandtiles().toArray()));
            }
            else{
                assertFalse(Arrays.equals(oldHand.toArray(), savedGame.getHands().get(1).getHandtiles().toArray()));
            }
        }
    }*/
}
