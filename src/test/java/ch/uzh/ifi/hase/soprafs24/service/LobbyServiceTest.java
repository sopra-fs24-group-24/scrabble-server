package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.HandRepository;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ScoreRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class LobbyServiceTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HandRepository handRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @InjectMocks
    private LobbyService lobbyService;

    private Lobby testLobby;
    private User testUser;
    private User testUser2;
    private User testUser3;
    private User testUser4;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // given
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUsername("Kevin");
        testUser2.setPassword("2");

        testUser3 = new User();
        testUser3.setId(3L);
        testUser3.setUsername("Manuel");
        testUser3.setPassword("3");

        testUser4 = new User();
        testUser4.setId(4L);
        testUser4.setUsername("Martin");
        testUser4.setPassword("4");

        testLobby = new Lobby();
        testLobby.setId(1L);
        testLobby.setLobbySize(4);
        testLobby.setNumberOfPlayers(1);
        List<Long> playersId = new ArrayList<Long>();
        playersId.add(1L);
        testLobby.setUsersInLobby(playersId);
        List<User> players = new ArrayList<>();
        players.add(testUser);
        testLobby.setGameStarted(false);

        // when
        Mockito.when(lobbyRepository.save(Mockito.any())).thenReturn(testLobby);
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(testUser));
        Mockito.when(userRepository.findById(2L)).thenReturn(Optional.ofNullable(testUser2));
        Mockito.when(userRepository.findById(3L)).thenReturn(Optional.ofNullable(testUser3));
        Mockito.when(userRepository.findById(4L)).thenReturn(Optional.ofNullable(testUser4));
    }

    @Test
    public void createLobby_validInputs_success() {
        // when
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // then
        Mockito.verify(lobbyRepository, Mockito.times(1)).save(Mockito.any());

        assertEquals(testLobby.getId(), createdLobby.getId());
        assertEquals(testLobby.getLobbySize(), createdLobby.getLobbySize());
        assertEquals(testLobby.getNumberOfPlayers(), createdLobby.getNumberOfPlayers());
        assertEquals(testLobby.getUsersInLobby(), createdLobby.getUsersInLobby());
        assertEquals(testUser, createdLobby.getPlayers().get(0));
        assertFalse(createdLobby.getGameStarted());
        assertNull(createdLobby.getGameOfLobby());
    }

    @Test
    public void createLobby_UserAlreadyInLobby_throwsException() {
        // given
        lobbyService.createLobby(testLobby);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(testLobby));

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.createLobby(testLobby));
    }

    @Test
    public void addPlayerToLobby_validInputs_thenPlayerAddedToLobby() {
        // given
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(null));
        Mockito.when(lobbyRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testLobby));
        lobbyService.addPlayertoLobby(1L, 2L);

        // then
        List<Long> expectedPlayersId = new ArrayList<>();
        expectedPlayersId.add(1L);
        expectedPlayersId.add(2L);

        List<User> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(testUser);
        expectedPlayers.add(testUser2);

        assertEquals(1L, createdLobby.getId());
        assertEquals(4, createdLobby.getLobbySize());
        assertEquals(2, createdLobby.getNumberOfPlayers());
        assertArrayEquals(expectedPlayersId.toArray(), createdLobby.getUsersInLobby().toArray());
        assertArrayEquals(expectedPlayers.toArray(), createdLobby.getPlayers().toArray());
        assertFalse(createdLobby.getGameStarted());
        assertNull(createdLobby.getGameOfLobby());
    }

    @Test
    public void addPlayerToLobby_validInputs_lobbyFull_thenGameIsStarted() {
        // given
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(null));
        Mockito.when(lobbyRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testLobby));
        lobbyService.addPlayertoLobby(1L, 2L);
        lobbyService.addPlayertoLobby(1L, 3L);
        lobbyService.addPlayertoLobby(1L, 4L);

        // then
        List<Long> expectedPlayersId = new ArrayList<>();
        expectedPlayersId.add(1L);
        expectedPlayersId.add(2L);
        expectedPlayersId.add(3L);
        expectedPlayersId.add(4L);

        List<User> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(testUser);
        expectedPlayers.add(testUser2);
        expectedPlayers.add(testUser3);
        expectedPlayers.add(testUser4);

        assertEquals(1L, createdLobby.getId());
        assertEquals(4, createdLobby.getLobbySize());
        assertEquals(4, createdLobby.getNumberOfPlayers());
        assertArrayEquals(expectedPlayersId.toArray(), createdLobby.getUsersInLobby().toArray());
        assertArrayEquals(expectedPlayers.toArray(), createdLobby.getPlayers().toArray());
        assertTrue(createdLobby.getGameStarted());
        assertNotNull(createdLobby.getGameOfLobby());
    }

    @Test
    public void addPlayerToLobby_NonExistentLobby_throwsException() {
        // given
        lobbyService.createLobby(testLobby);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(null));
        Mockito.when(lobbyRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(null));

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(3L, 2L));
    }

    @Test
    public void addPlayerToLobby_UserAlreadyInLobby_throwsException() {
        // given
        lobbyService.createLobby(testLobby);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(testLobby));

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(1L, 2L));
    }

    @Test
    public void addPlayerToLobby_LobbyAlreadyFull_throwsException() {
        // given
        Lobby createdLobby = lobbyService.createLobby(testLobby);
        createdLobby.setNumberOfPlayers(4);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(null));
        Mockito.when(lobbyRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(createdLobby));

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(1L, 2L));
    }

    @Test
    public void removeHandsFromOtherPlayers_lobbyNotFull_nothingIsDone(){
        // given
        LobbyGetDTO testLobbyGetDTO = new LobbyGetDTO();
        testLobbyGetDTO.setGameStarted(false);
        Long userId = 1L;

        // when/then
        assertDoesNotThrow(() -> {lobbyService.removeHandsFromOtherPlayers(testLobbyGetDTO, userId);});
    }

    @Test
    public void removeHandsFromOtherPlayers_lobbyFull_success(){
        // given
        LobbyGetDTO testLobby = new LobbyGetDTO();

        Hand hand1 = new Hand();
        hand1.setId(3L);
        hand1.setHanduserid(1L);

        Tile tile1 = new Tile('K', 3);
        Tile tile2 = new Tile('J', 4);
        Tile tile3 = new Tile('A', 1);
        Tile tile4 = new Tile('A', 1);
        Tile tile5 = new Tile('E', 1);
        Tile tile6 = new Tile('E', 1);
        Tile tile7 = new Tile('N', 2);

        List<Tile> tiles1 = new ArrayList<>();
        tiles1.add(tile1);
        tiles1.add(tile2);
        tiles1.add(tile3);
        tiles1.add(tile4);
        tiles1.add(tile5);
        tiles1.add(tile6);
        tiles1.add(tile7);

        hand1.setHandtiles(tiles1);

        Hand hand2 = new Hand();
        hand2.setId(4L);
        hand2.setHanduserid(2L);

        Tile tile8 = new Tile('M', 3);
        Tile tile9 = new Tile('I', 2);
        Tile tile10 = new Tile('A', 1);
        Tile tile11 = new Tile('O', 3);
        Tile tile12 = new Tile('E', 1);
        Tile tile13 = new Tile('L', 1);
        Tile tile14 = new Tile('C', 2);

        List<Tile> tiles2 = new ArrayList<>();
        tiles2.add(tile8);
        tiles2.add(tile9);
        tiles2.add(tile10);
        tiles2.add(tile11);
        tiles2.add(tile12);
        tiles2.add(tile13);
        tiles2.add(tile14);

        hand2.setHandtiles(tiles2);

        List<Hand> hands = new ArrayList<>();
        hands.add(hand1);
        hands.add(hand2);

        testLobby.setGameStarted(true);
        GameGetDTO game = new GameGetDTO();
        game.setHands(hands);
        testLobby.setGameOfLobby(game);

        Long userId = 1L;

        //when
        lobbyService.removeHandsFromOtherPlayers(testLobby, userId);

        //then
        assertEquals(1, testLobby.getGameOfLobby().getHands().size());
        assertEquals(1L, testLobby.getGameOfLobby().getHands().get(0).getHanduserid());
        assertEquals(7, testLobby.getGameOfLobby().getHands().get(0).getHandtiles().size());
        assertArrayEquals(hand1.getHandtiles().toArray(), testLobby.getGameOfLobby().getHands().get(0).getHandtiles().toArray());
    }

    @Test
    public void removeHandsFromOtherPlayers_lobbyFull_noHandWithIndicatedUserId_error(){
        // given
        LobbyGetDTO testLobby = new LobbyGetDTO();

        Hand hand1 = new Hand();
        hand1.setId(3L);
        hand1.setHanduserid(1L);

        Tile tile1 = new Tile('K', 3);
        Tile tile2 = new Tile('J', 4);
        Tile tile3 = new Tile('A', 1);
        Tile tile4 = new Tile('A', 1);
        Tile tile5 = new Tile('E', 1);
        Tile tile6 = new Tile('E', 1);
        Tile tile7 = new Tile('N', 2);

        List<Tile> tiles1 = new ArrayList<>();
        tiles1.add(tile1);
        tiles1.add(tile2);
        tiles1.add(tile3);
        tiles1.add(tile4);
        tiles1.add(tile5);
        tiles1.add(tile6);
        tiles1.add(tile7);

        hand1.setHandtiles(tiles1);

        Hand hand2 = new Hand();
        hand2.setId(4L);
        hand2.setHanduserid(2L);

        Tile tile8 = new Tile('M', 3);
        Tile tile9 = new Tile('I', 2);
        Tile tile10 = new Tile('A', 1);
        Tile tile11 = new Tile('O', 3);
        Tile tile12 = new Tile('E', 1);
        Tile tile13 = new Tile('L', 1);
        Tile tile14 = new Tile('C', 2);

        List<Tile> tiles2 = new ArrayList<>();
        tiles2.add(tile8);
        tiles2.add(tile9);
        tiles2.add(tile10);
        tiles2.add(tile11);
        tiles2.add(tile12);
        tiles2.add(tile13);
        tiles2.add(tile14);

        hand2.setHandtiles(tiles2);

        List<Hand> hands = new ArrayList<>();
        hands.add(hand1);
        hands.add(hand2);

        testLobby.setGameStarted(true);
        GameGetDTO game = new GameGetDTO();
        game.setHands(hands);
        testLobby.setGameOfLobby(game);

        Long userId = 5L;

        // when/then
        assertThrows(ResponseStatusException.class, () -> lobbyService.removeHandsFromOtherPlayers(testLobby, userId));
    }

    @Test
    public void transformUsersIntoUsersSlim_Success(){
        // given
        LobbyGetDTO testLobbyGetDTO = new LobbyGetDTO();

        Lobby testLobby = new Lobby();
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("fabio");
        user1.setToken("1");
        user1.setStatus(UserStatus.ONLINE);
        user1.setPassword("1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("luca");
        user2.setToken("2");
        user2.setStatus(UserStatus.ONLINE);
        user2.setPassword("2");

        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("elena");
        user3.setToken("3");
        user3.setStatus(UserStatus.ONLINE);
        user3.setPassword("3");

        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);
        testLobby.setPlayers(players);

        // when
        lobbyService.transformUsersIntoUsersSlim(testLobbyGetDTO, testLobby);
        List<String> expectedFields = new ArrayList<>();
        expectedFields.add("id");
        expectedFields.add("username");
        expectedFields.add("status");

        User expectedUser = new User();
        expectedUser.setStatus(UserStatus.ONLINE);

        // then
        assertEquals(3, testLobbyGetDTO.getPlayers().size());
        assertEquals(3, testLobbyGetDTO.getPlayers().get(0).getClass().getDeclaredFields().length);
        assertEquals(3, testLobbyGetDTO.getPlayers().get(1).getClass().getDeclaredFields().length);
        assertEquals(3, testLobbyGetDTO.getPlayers().get(2).getClass().getDeclaredFields().length);

        List<String> fieldsPlayer1 = new ArrayList<>();
        for (Field field : testLobbyGetDTO.getPlayers().get(0).getClass().getDeclaredFields()){
            fieldsPlayer1.add(field.getName());
        }
        assertTrue(expectedFields.contains(fieldsPlayer1.get(0)));
        assertTrue(expectedFields.contains(fieldsPlayer1.get(1)));
        assertTrue(expectedFields.contains(fieldsPlayer1.get(2)));
        assertEquals("fabio", testLobbyGetDTO.getPlayers().get(0).getUsername());
        assertEquals(1L, testLobbyGetDTO.getPlayers().get(0).getId());
        assertEquals(expectedUser.getStatus(), testLobbyGetDTO.getPlayers().get(0).getStatus());

        List<String> fieldsPlayer2 = new ArrayList<>();
        for (Field field : testLobbyGetDTO.getPlayers().get(1).getClass().getDeclaredFields()){
            fieldsPlayer2.add(field.getName());
        }
        assertTrue(expectedFields.contains(fieldsPlayer2.get(0)));
        assertTrue(expectedFields.contains(fieldsPlayer2.get(1)));
        assertTrue(expectedFields.contains(fieldsPlayer2.get(2)));
        assertEquals("luca", testLobbyGetDTO.getPlayers().get(1).getUsername());
        assertEquals(2L, testLobbyGetDTO.getPlayers().get(1).getId());
        assertEquals(expectedUser.getStatus(), testLobbyGetDTO.getPlayers().get(1).getStatus());

        List<String> fieldsPlayer3 = new ArrayList<>();
        for (Field field : testLobbyGetDTO.getPlayers().get(2).getClass().getDeclaredFields()){
            fieldsPlayer3.add(field.getName());
        }
        assertTrue(expectedFields.contains(fieldsPlayer3.get(0)));
        assertTrue(expectedFields.contains(fieldsPlayer3.get(1)));
        assertTrue(expectedFields.contains(fieldsPlayer3.get(2)));
        assertEquals("elena", testLobbyGetDTO.getPlayers().get(2).getUsername());
        assertEquals(3L, testLobbyGetDTO.getPlayers().get(2).getId());
        assertEquals(expectedUser.getStatus(), testLobbyGetDTO.getPlayers().get(2).getStatus());
    }

    @Test
    public void removePlayer_success() {
        // given
        Game game = new Game();
        game.setCurrentPlayer(1L);

        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("fabio");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("luca");
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("martin");

        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user2);
        game.setPlayers(players);

        Hand hand1 = new Hand();
        hand1.setHanduserid(1L);
        List<Tile> handTiles1 = new ArrayList<>();
        handTiles1.add(new Tile('A', 1));
        handTiles1.add(new Tile('A', 1));
        handTiles1.add(new Tile('A', 1));
        handTiles1.add(new Tile('A', 1));
        handTiles1.add(new Tile('A', 1));
        handTiles1.add(new Tile('A', 1));
        handTiles1.add(new Tile('A', 1));
        hand1.setHandtiles(handTiles1);
        int id = 1;
        for (int i = 0; i < 7; i++){
            hand1.getHandtiles().get(i).setId((long) id);
            id++;
        }

        Hand hand2 = new Hand();
        hand2.setHanduserid(2L);
        List<Tile> handTiles2 = new ArrayList<>();
        handTiles2.add(new Tile('B', 1));
        handTiles2.add(new Tile('B', 1));
        handTiles2.add(new Tile('B', 1));
        handTiles2.add(new Tile('B', 1));
        handTiles2.add(new Tile('B', 1));
        handTiles2.add(new Tile('B', 1));
        handTiles2.add(new Tile('B', 1));
        hand2.setHandtiles(handTiles2);
        for (int i = 0; i < 7; i++){
            hand2.getHandtiles().get(i).setId((long) id);
            id++;
        }

        Hand hand3 = new Hand();
        hand3.setHanduserid(3L);
        List<Tile> handTiles3 = new ArrayList<>();
        handTiles3.add(new Tile('C', 1));
        handTiles3.add(new Tile('C', 1));
        handTiles3.add(new Tile('C', 1));
        handTiles3.add(new Tile('C', 1));
        handTiles3.add(new Tile('C', 1));
        handTiles3.add(new Tile('C', 1));
        handTiles3.add(new Tile('C', 1));
        hand3.setHandtiles(handTiles3);
        for (int i = 0; i < 7; i++){
            hand3.getHandtiles().get(i).setId((long) id);
            id++;
        }

        List<Hand> hands = new ArrayList<>();
        hands.add(hand1);
        hands.add(hand2);
        hands.add(hand3);
        game.setHands(hands);

        Score score1 = new Score();
        score1.setScoreUserId(1L);
        score1.setScore(10);

        Score score2 = new Score();
        score2.setScoreUserId(2L);
        score2.setScore(20);

        Score score3 = new Score();
        score3.setScoreUserId(3L);
        score3.setScore(30);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        scores.add(score3);
        game.setScores(scores);

        Bag bag = new Bag();
        bag.setId(100L);
        List<Tile> tilesInBag = new ArrayList<>();
        tilesInBag.add(new Tile('D', 2));
        tilesInBag.add(new Tile('D', 2));
        tilesInBag.add(new Tile('D', 2));
        bag.setTiles(tilesInBag);
        game.setBag(bag);

        when(handRepository.findById(Mockito.any())).thenReturn(Optional.of(hand1));
        when(scoreRepository.findById(Mockito.any())).thenReturn(Optional.of(score1));

        // when
        lobbyService.removePlayer(game, user1);

        // then
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(user2.getId());
        expectedPlayers.add(user3.getId());

        List<Long> expectedHands = new ArrayList<>();
        expectedHands.add(hand2.getHanduserid());
        expectedHands.add(hand3.getHanduserid());

        List<Long> expectedScores = new ArrayList<>();
        expectedScores.add(score2.getScoreUserId());
        expectedScores.add(score3.getScoreUserId());

        assertEquals(2, game.getPlayers().size());
        assertTrue(expectedPlayers.contains(game.getPlayers().get(0).getId()));
        assertTrue(expectedPlayers.contains(game.getPlayers().get(1).getId()));
        assertEquals(2, game.getHands().size());
        assertTrue(expectedHands.contains(game.getHands().get(0).getHanduserid()));
        assertTrue(expectedHands.contains(game.getHands().get(1).getHanduserid()));
        assertEquals(2, game.getScores().size());
        assertTrue(expectedScores.contains(game.getScores().get(0).getScoreUserId()));
        assertTrue(expectedScores.contains(game.getScores().get(1).getScoreUserId()));
        assertTrue(expectedPlayers.contains(game.getCurrentPlayer()));

        if (game.getHands().get(0).getHanduserid() == hand2.getHanduserid()){
            assertArrayEquals(hand2.getHandtiles().toArray(), game.getHands().get(0).getHandtiles().toArray());
            assertArrayEquals(hand3.getHandtiles().toArray(), game.getHands().get(1).getHandtiles().toArray());
        }
        else {
            assertArrayEquals(hand3.getHandtiles().toArray(), game.getHands().get(0).getHandtiles().toArray());
            assertArrayEquals(hand2.getHandtiles().toArray(), game.getHands().get(1).getHandtiles().toArray());
        }

        if (game.getScores().get(0).getScoreUserId() == score2.getScoreUserId()){
            assertEquals(20, game.getScores().get(0).getScore());
            assertEquals(30, game.getScores().get(1).getScore());
        }
        else{
            assertEquals(30, game.getScores().get(0).getScore());
            assertEquals(20, game.getScores().get(1).getScore());
        }
    }
}
