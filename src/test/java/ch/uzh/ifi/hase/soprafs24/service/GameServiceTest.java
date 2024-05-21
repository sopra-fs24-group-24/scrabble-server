package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.dictionary.Dictionary;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.BagRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.HandRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ScoreRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.GameGetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private Dictionary dictionary;

    @Mock
    private HandRepository handRepository;

    @Mock
    private ScoreRepository scoreRepository;

    @Mock
    private BagRepository bagRepository;

    @Mock
    private Bag bag;

    @Mock
    private HttpResponse<String> mockResponse;

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private List<User> testUsers = new ArrayList<>();
    private Score testScore1;
    private Score testScore2;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        Tile tile1 = new Tile('A', 1);
        Tile tile2 = new Tile('B', 2);
        Tile tile3 = new Tile('C', 3);

        List<Tile> tiles = new ArrayList<>();
        tiles.add(tile1);
        tiles.add(tile2);
        tiles.add(tile3);

        bag.setTiles(tiles);

        testGame = new Game();
        testGame.setId(1L);
        testGame.setBag(bag);

        User testUser1 = new User();
        testUser1.setId(1L);
        User testUser2 = new User();
        testUser2.setId(2L);

        testUsers.add(testUser1);
        testUsers.add(testUser2);

        Hand testHand1 = new Hand();
        testHand1.setHanduserid(1L);
        testHand1.setHandtiles(new ArrayList<>());
        Hand testHand2 = new Hand();
        testHand2.setHanduserid(2L);

        List<Hand> hands = new ArrayList<>();
        hands.add(testHand1);
        hands.add(testHand2);

        testGame.setHands(hands);
        testGame.setPlayers(testUsers);
        testGame.setCurrentPlayer(1L);

        testScore1 = new Score();
        testScore1.setId(1L);
        testScore1.setScore(0);

        testScore2 = new Score();
        testScore2.setId(2L);
        testScore2.setScore(0);

        when(scoreRepository.findByScoreUserId(Mockito.any())).thenReturn(testScore1);
        when(bagRepository.findById(Mockito.any())).thenReturn(Optional.of(bag));
        when(handRepository.findByHanduserid(Mockito.any())).thenReturn(testHand1);

        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
    }

    @Test
    public void getGameData_gameWithIndicatedGameIdExists_returnGameData() {
        // given
        testGame.setId(1L);

        // when
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        // then
        assertDoesNotThrow(() -> gameService.getGameParams(1L));
    }

    @Test
    public void getGameData_gameWithIndicatedGameIdDoesNotExist_throwError() {
        // given
        testGame.setId(2L);

        // when
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());

        // then
        assertThrows(ResponseStatusException.class, () -> gameService.getGameParams(1L));
    }

    @Test
    public void skipTurn_2Players_success() {
        User user = testGame.getPlayers().get(0);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        gameService.skipTurn(user, 1L);
        assertEquals(2L, testGame.getCurrentPlayer());
    }

    @Test
    public void nextPlayer_2Players_success() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        gameService.makeNextPlayerToCurrentPlayer(1L);
        assertEquals(2L, testGame.getCurrentPlayer());
    }

    @Test
    public void nextPlayer_3Players_success() {
        User testUser3 = new User();
        testUser3.setId(3L);
        testGame.getPlayers().add(testUser3);
        testGame.setCurrentPlayer(3L);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        gameService.makeNextPlayerToCurrentPlayer(1L);
        assertEquals(1L, testGame.getCurrentPlayer());
    }

    @Test
    public void nextPlayer_wrongId_fail() {
        when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class ,() -> gameService.makeNextPlayerToCurrentPlayer(2L));
    }

    @Test
    public void authenticatePlayer_invalidGameId_fail() {
        User user = testGame.getPlayers().get(0);

        when(gameRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class ,() -> gameService.authenticateUserForMove(user, 2L));
    }

    @Test
    public void authenticatePlayer_invalidUserId_fail() {
        User user = testGame.getPlayers().get(1);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        assertThrows(ResponseStatusException.class ,() -> gameService.authenticateUserForMove(user, 1L));
    }

    @Test
    public void authenticatePlayer_success() {
        User user = testGame.getPlayers().get(0);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        assertDoesNotThrow(() -> gameService.authenticateUserForMove(user, 1L));
    }

    @Test
    public void validateWord_isValid() {
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        assertTrue(gameService.validateWord("testWord"));
    }

    @Test
    public void validateWord_isInvalid() {
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);

        assertFalse(gameService.validateWord("testWord"));
    }

    @Test
    public void validateWord_throwsException() {
        ResponseStatusException response = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        doThrow(response).when(dictionary).getScrabbleScore(Mockito.any());
        assertThrows(ResponseStatusException.class, () -> gameService.validateWord("testWord"));
    }

    @Test
    public void getScrabbleScore_wordIsValid() {
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{" + '"' + "value" + '"' + ":8}");

        assertEquals(8, gameService.getScrabbleScore("testWord"));
    }

    @Test
    public void getScrabbleScore_wordIsInvalid() {
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);

        assertEquals(0, gameService.getScrabbleScore("testWord"));
    }

    @Test
    public void getScrabbleScore_throwsException() {
        ResponseStatusException response = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        doThrow(response).when(dictionary).getScrabbleScore(Mockito.any());

        assertThrows(ResponseStatusException.class, () -> gameService.getScrabbleScore("testWord"));
    }

    @Test
    public void getWordDefinition_OneWord() {
        when(dictionary.getWordDefinition(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("[{" + '"' + "text" + '"' + ": " + '"' + "<xref>Definition</xref>" + '"' + "}]");

        List<String> words = new ArrayList<>();
        words.add("Word");

        Map<String, String> definitions = gameService.getDefinition(words);

        assertTrue(definitions.containsKey("Word"));
        assertEquals("Definition", definitions.get("Word"));
    }

    @Test
    public void getWordDefinition_TwoWords() {
        when(dictionary.getWordDefinition(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.body())
                .thenReturn("[{" + '"' + "text" + '"' + ": " + '"' + "<xref>Definition1</xref>" + '"' + "}]")
                .thenReturn("[{" + '"' + "text" + '"' + ": " + '"' + "<xref>Definition2</xref>" + '"' + "}]");

        List<String> words = new ArrayList<>();
        words.add("Word1");
        words.add("Word2");

        Map<String, String> definitions = gameService.getDefinition(words);

        assertTrue(definitions.containsKey("Word1"));
        assertTrue(definitions.containsKey("Word2"));
        assertEquals("Definition1", definitions.get("Word1"));
        assertEquals("Definition2", definitions.get("Word2"));
    }

    @Test
    public void getWordDefinition_NoDefinition() {
        when(dictionary.getWordDefinition(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn("[{" + '"' + "citations" + '"' + ": []},{" + '"' + "citations" + '"' + ": []}]");

        List<String> words = new ArrayList<>();
        words.add("Word");

        Map<String, String> definitions = gameService.getDefinition(words);

        assertTrue(definitions.containsKey("Word"));
        assertEquals("No definition found.", definitions.get("Word"));
    }

    @Test
    public void removeHandsFromOtherPlayers_Success(){
        //given
        GameGetDTO testGame = new GameGetDTO();

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
        testGame.setHands(hands);

        Long userId = 1L;

        //when
        gameService.removeHandsFromOtherPlayers(testGame, userId);

        //then
        assertEquals(1, testGame.getHands().size());
        assertEquals(1L, testGame.getHands().get(0).getHanduserid());
        assertEquals(7, testGame.getHands().get(0).getHandtiles().size());
        assertArrayEquals(hand1.getHandtiles().toArray(), testGame.getHands().get(0).getHandtiles().toArray());
    }

    @Test
    public void removeHandsFromOtherPlayers_NoHandWithIndicatedUserId_Error(){
        // given
        GameGetDTO testGame = new GameGetDTO();

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
        testGame.setHands(hands);

        Long userId = 3L;

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.removeHandsFromOtherPlayers(testGame, userId));
    }

    @Test
    public void transformUsersIntoUsersSlim_Success(){
        // given
        GameGetDTO testGameGetDTO = new GameGetDTO();

        Game testGame = new Game();
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
        testGame.setPlayers(players);

        // when
        gameService.transformUsersIntoUsersSlim(testGameGetDTO, testGame);
        List<String> expectedFields = new ArrayList<>();
        expectedFields.add("id");
        expectedFields.add("username");
        expectedFields.add("status");

        User expectedUser = new User();
        expectedUser.setStatus(UserStatus.ONLINE);

        // then
        assertEquals(3, testGameGetDTO.getPlayers().size());
        assertEquals(3, testGameGetDTO.getPlayers().get(0).getClass().getDeclaredFields().length);
        assertEquals(3, testGameGetDTO.getPlayers().get(1).getClass().getDeclaredFields().length);
        assertEquals(3, testGameGetDTO.getPlayers().get(2).getClass().getDeclaredFields().length);

        List<String> fieldsPlayer1 = new ArrayList<>();
        for (Field field : testGameGetDTO.getPlayers().get(0).getClass().getDeclaredFields()){
            fieldsPlayer1.add(field.getName());
        }
        assertTrue(expectedFields.contains(fieldsPlayer1.get(0)));
        assertTrue(expectedFields.contains(fieldsPlayer1.get(1)));
        assertTrue(expectedFields.contains(fieldsPlayer1.get(2)));
        assertEquals("fabio", testGameGetDTO.getPlayers().get(0).getUsername());
        assertEquals(1L, testGameGetDTO.getPlayers().get(0).getId());
        assertEquals(expectedUser.getStatus(), testGameGetDTO.getPlayers().get(0).getStatus());

        List<String> fieldsPlayer2 = new ArrayList<>();
        for (Field field : testGameGetDTO.getPlayers().get(1).getClass().getDeclaredFields()){
            fieldsPlayer2.add(field.getName());
        }
        assertTrue(expectedFields.contains(fieldsPlayer2.get(0)));
        assertTrue(expectedFields.contains(fieldsPlayer2.get(1)));
        assertTrue(expectedFields.contains(fieldsPlayer2.get(2)));
        assertEquals("luca", testGameGetDTO.getPlayers().get(1).getUsername());
        assertEquals(2L, testGameGetDTO.getPlayers().get(1).getId());
        assertEquals(expectedUser.getStatus(), testGameGetDTO.getPlayers().get(1).getStatus());

        List<String> fieldsPlayer3 = new ArrayList<>();
        for (Field field : testGameGetDTO.getPlayers().get(2).getClass().getDeclaredFields()){
            fieldsPlayer3.add(field.getName());
        }
        assertTrue(expectedFields.contains(fieldsPlayer3.get(0)));
        assertTrue(expectedFields.contains(fieldsPlayer3.get(1)));
        assertTrue(expectedFields.contains(fieldsPlayer3.get(2)));
        assertEquals("elena", testGameGetDTO.getPlayers().get(2).getUsername());
        assertEquals(3L, testGameGetDTO.getPlayers().get(2).getId());
        assertEquals(expectedUser.getStatus(), testGameGetDTO.getPlayers().get(2).getStatus());
    }

    @Test
    public void wordContested_wordInvalid_scoresUpdated() {
        List<Score> scores = new ArrayList<>();
        testScore1.setScore(50);
        testScore1.setScoreUserId(1L);
        testScore2.setScore(50);
        testScore2.setScoreUserId(2L);
        scores.add(testScore1);
        scores.add(testScore2);
        testGame.setScores(scores);

        Map<Long, Boolean> contested = new HashMap<>();
        contested.put(2L, true);

        Map<String, Integer> words = new HashMap<>();
        words.put("test", 10);

        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.of(testGame));
        when(mockResponse.statusCode()).thenReturn(404);

        gameService.changeScoresAfterContesting(testGame, contested, words);

        assertEquals(40, testGame.getScores().get(0).getScore());
        assertEquals(70, testGame.getScores().get(1).getScore());
    }

    @Test
    public void wordContested_wordValid_scoresUpdated() {
        List<Score> scores = new ArrayList<>();
        testScore1.setScore(50);
        testScore1.setScoreUserId(1L);
        testScore2.setScore(50);
        testScore2.setScoreUserId(2L);
        scores.add(testScore1);
        scores.add(testScore2);
        testGame.setScores(scores);

        Map<Long, Boolean> contested = new HashMap<>();
        contested.put(2L, true);

        Map<String, Integer> words = new HashMap<>();
        words.put("test", 10);

        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.of(testGame));

        gameService.changeScoresAfterContesting(testGame, contested, words);

        assertEquals(80, testGame.getScores().get(0).getScore());
        assertEquals(30, testGame.getScores().get(1).getScore());
    }

    @Test
    public void userPlayfieldTooBig_throwError() {
        // given
        List<Tile> generatedPlayfield = new ArrayList<>();

        for (int i = 0; i < 225; i++) {
            generatedPlayfield.add(i, null);
        }

        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 227; i++){
            userPlayfield.add(null);
        }
        Tile tile1 = new Tile('A', 1);
        tile1.setId(225L);
        tile1.setBoardidx(225);
        Tile tile2 = new Tile('A', 1);
        tile2.setId(226L);
        tile2.setBoardidx(226);
        userPlayfield.set(225, tile1);
        userPlayfield.set(226, tile2);
        userGame.setPlayfield(userPlayfield);

        // when/then
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        assertThrows(ResponseStatusException.class, () -> gameService.placeTilesOnBoard(userGame));
    }

    @Test
    public void contestWord_userNotPartOfGame_throwError() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(4L);


        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);

        testGame.setId(3L);
        testGame.setPlayers(players);

        // when/then
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        assertThrows(ResponseStatusException.class, () -> gameService.contestWord(testGame.getId(), user3, true));
    }

    @Test
    public void allTilesPlayed_validMove_saveUpdatedPlayfield() {
        List<Tile> generatedPlayfield = new ArrayList<>();

        for (int i = 0; i < 225; i++) {
            generatedPlayfield.add(i, null);
        }

        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        Game userGame = new Game();
        char[] lettersUser = {'A', 'A', 'A', 'A', 'A', 'A', 'A'};
        int[] valuesUser = {3, 3, 3, 3, 3, 3, 3};
        int[] boardIndicesUser = {109, 110, 111, 112, 113, 114, 115};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        char[] lettersExpected = {'A', 'A', 'A', 'A', 'A', 'A', 'A'};
        int[] valuesExpected = {3, 3, 3, 3, 3, 3, 3};
        int[] boardIndicesExpected = {109, 110, 111, 112, 113, 114, 115};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        List<Tile> expectedOldPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedOldPlayfield.add(i, null);
        }

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("AAAAAAA"));
    }

    @Test
    public void firstWordPlaced_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T'};
        int[] valuesUser = {4, 3, 5};
        int[] boardIndicesUser = {112, 127, 142};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T'};
        int[] valuesExpected = {4, 3, 5};
        int[] boardIndicesExpected = {112, 127, 142};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        List<Tile> expectedOldPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedOldPlayfield.add(i, null);
        }

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAT"));
    }

    @Test
    public void wordIsPlacedHorizontallyOnPlayfield_validMove_saveUpdatedPlayfield() {
        /* In this test, a new word is placed horizontally to the right of
        an existing tile*/

        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 127, 142};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 143, 144};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesExpected = {4, 3, 5, 2, 3};
        int[] boardIndicesExpected = {112, 127, 142, 143, 144};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {112, 127, 142};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("TEA"));
    }

    @Test
    public void wordIsPlacedHorizontallyOnPlayfield2_validMove_saveUpdatedPlayfield() {
        /* In this test, a new word is placed horizontally, whereby at least 2 new tiles
        are connected by an existing tile*/

        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndices = {112, 127, 142};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndices);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 126, 128};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesExpected = {4, 3, 5, 2, 3};
        int[] boardIndicesExpected = {112, 127, 142, 126, 128};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {112, 127, 142};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("EAA"));
    }

    @Test
    public void wordIsPlacedHorizontallyAndParallelToExistingWord_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 113, 114};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'D', 'O', 'G'};
        int[] valuesUser = {4, 3, 5, 2, 6, 4};
        int[] boardIndicesUser = {112, 113, 114, 128, 129, 130};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'D', 'O', 'G'};
        int[] valuesExpected = {4, 3, 5, 2, 6, 4};
        int[] boardIndicesExpected = {112, 113, 114, 128, 129, 130};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {112, 113, 114};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(3, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("AD"));
        assertTrue(testGame.getWordsToBeContested().contains("TO"));
        assertTrue(testGame.getWordsToBeContested().contains("DOG"));
    }

    @Test
    public void wordIsPlacedVerticallyOnPlayfield_validMove_saveUpdatedPlayfield() {
        /* In this test, a new word is placed vertically where the last tile is
        connected to an existing word*/

        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 113, 114};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A'};
        int[] valuesUser = {4, 3, 5, 4, 3};
        int[] boardIndicesUser = {112, 113, 114, 82, 97};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A'};
        int[] valuesExpected = {4, 3, 5, 4, 3};
        int[] boardIndicesExpected = {112, 113, 114, 82, 97};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {112, 113, 114};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAC"));
    }

    @Test
    public void newWordIsPlacedVerticallyAcrossExistingWord_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {111, 112, 113};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 7};
        int[] boardIndicesUser = {111, 112, 113, 97, 127};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 7};
        int[] boardIndicesExpected = {111, 112, 113, 97, 127};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {111, 112, 113};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAR"));
    }

    @Test
    public void newWordIsPlacedVerticallyAndParallelToExistingWord_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {97, 112, 127};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesUser = {97, 112, 127, 113, 128, 143};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesExpected = {97, 112, 127, 113, 128, 143};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {97, 112, 127};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(3, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAR"));
        assertTrue(testGame.getWordsToBeContested().contains("AC"));
        assertTrue(testGame.getWordsToBeContested().contains("TA"));
    }

    @Test
    public void newWordIsPlacedHorizontallyOnFirstRow_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T', 'S', 'U', 'N'};
        int[] valuesGenerated = {4, 3, 5, 5, 4, 3};
        int[] boardIndicesGenerated = {1, 16, 31, 4, 19, 34};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'S', 'U', 'N', 'A', 'R'};
        int[] valuesUser = {4, 3, 5, 5, 4, 3, 3, 7};
        int[] boardIndicesUser = {1, 16, 31, 4, 19, 34, 2, 3};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'S', 'U', 'N', 'A', 'R'};
        int[] valuesExpected = {4, 3, 5, 5, 4, 3, 3, 7};
        int[] boardIndicesExpected = {1, 16, 31, 4, 19, 34, 2, 3};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T', 'S', 'U', 'N'};
        int[] valuesOldExpected = {4, 3, 5, 5, 4, 3};
        int[] boardIndicesOldExpected = {1, 16, 31, 4, 19, 34};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CARS"));
    }

    @Test
    public void newWordIsPlacedHorizontallyOnFirstRow2_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {15, 16, 17};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesUser = {15, 16, 17, 0, 1, 2};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesExpected = {15, 16, 17, 0, 1, 2};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {15, 16, 17};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(4, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAR"));
        assertTrue(testGame.getWordsToBeContested().contains("CC"));
        assertTrue(testGame.getWordsToBeContested().contains("AA"));
        assertTrue(testGame.getWordsToBeContested().contains("RT"));
    }

    @Test
    public void newWordIsPlacedHorizontallyOnFirstRow3_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {27, 28, 29};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesUser = {27, 28, 29, 12, 13, 14};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesExpected = {27, 28, 29, 12, 13, 14};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {27, 28, 29};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(4, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAR"));
        assertTrue(testGame.getWordsToBeContested().contains("CC"));
        assertTrue(testGame.getWordsToBeContested().contains("AA"));
        assertTrue(testGame.getWordsToBeContested().contains("RT"));
    }

    @Test
    public void newWordIsPlacedVerticallyOnFirstColumn_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'R', 'S', 'T', 'O', 'N', 'E'};
        int[] valuesGenerated = {4, 1, 5, 4, 4, 3, 2, 1};
        int[] boardIndicesGenerated = {30, 31, 32, 75, 76, 77, 78, 79};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'R', 'S', 'T', 'O', 'N', 'E', 'A', 'T'};
        int[] valuesUser = {4, 1, 5, 4, 4, 3, 2, 1, 1, 4};
        int[] boardIndicesUser = {30, 31, 32, 75, 76, 77, 78, 79, 45, 60};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'R', 'S', 'T', 'O', 'N', 'E', 'A', 'T'};
        int[] valuesExpected = {4, 1, 5, 4, 4, 3, 2, 1, 1, 4};
        int[] boardIndicesExpected = {30, 31, 32, 75, 76, 77, 78, 79, 45, 60};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'R', 'S', 'T', 'O', 'N', 'E'};
        int[] valuesOldExpected = {4, 1, 5, 4, 4, 3, 2, 1};
        int[] boardIndicesOldExpected = {30, 31, 32, 75, 76, 77, 78, 79};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CATS"));
    }

    @Test
    public void newWordIsPlacedVerticallyOnFirstColumn2_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'S', 'T', 'O', 'N', 'E'};
        int[] valuesGenerated = {4, 4, 3, 2, 1};
        int[] boardIndicesGenerated = {45, 46, 47, 48, 49};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'R', 'S', 'T', 'O', 'N', 'E'};
        int[] valuesUser = {4, 1, 5, 4, 4, 3, 2, 1};
        int[] boardIndicesUser = {0, 15, 30, 45, 46, 47, 48, 49};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'R', 'S', 'T', 'O', 'N', 'E'};
        int[] valuesExpected = {4, 1, 5, 4, 4, 3, 2, 1};
        int[] boardIndicesExpected = {0, 15, 30, 45, 46, 47, 48, 49};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'S', 'T', 'O', 'N', 'E'};
        int[] valuesOldExpected = {4, 4, 3, 2, 1};
        int[] boardIndicesOldExpected = {45, 46, 47, 48, 49};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CARS"));
    }

    @Test
    public void newWordIsPlacedVerticallyOnFirstColumn3_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'R'};
        int[] valuesGenerated = {4, 1, 5};
        int[] boardIndicesGenerated = {180, 181, 182};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'R', 'A', 'T'};
        int[] valuesUser = {4, 1, 5, 1, 5};
        int[] boardIndicesUser = {180, 181, 182, 195, 210};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'R', 'A', 'T'};
        int[] valuesExpected = {4, 1, 5, 1, 5};
        int[] boardIndicesExpected = {180, 181, 182, 195, 210};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'R'};
        int[] valuesOldExpected = {4, 1, 5};
        int[] boardIndicesOldExpected = {180, 181, 182};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAT"));
    }

    @Test
    public void newWordIsPlacedHorizontallyOnLastRow_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {195, 196, 197};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesUser = {195, 196, 197, 210, 211, 212};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesExpected = {195, 196, 197, 210, 211, 212};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {195, 196, 197};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(4, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAR"));
        assertTrue(testGame.getWordsToBeContested().contains("CC"));
        assertTrue(testGame.getWordsToBeContested().contains("AA"));
        assertTrue(testGame.getWordsToBeContested().contains("TR"));
    }

    @Test
    public void newWordIsPlacedHorizontallyOnLastRow2_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {207, 208, 209};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesUser = {207, 208, 209, 222, 223, 224};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesExpected = {207, 208, 209, 222, 223, 224};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'T'};
        int[] valuesOldExpected = {4, 3, 5};
        int[] boardIndicesOldExpected = {207, 208, 209};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(4, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAR"));
        assertTrue(testGame.getWordsToBeContested().contains("CC"));
        assertTrue(testGame.getWordsToBeContested().contains("AA"));
        assertTrue(testGame.getWordsToBeContested().contains("TR"));
    }

    @Test
    public void newWordIsPlacedHorizontallyOnLastRow3_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesGenerated = {4, 3, 7, 7, 5, 4};
        int[] boardIndicesGenerated = {182, 197, 212, 185, 200, 215};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'A', 'I', 'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesUser = {3, 3, 4, 3, 7, 7, 5, 4};
        int[] boardIndicesUser = {213, 214, 182, 197, 212, 185, 200, 215};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'A', 'I', 'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesExpected = {3, 3, 4, 3, 7, 7, 5, 4};
        int[] boardIndicesExpected = {213, 214, 182, 197, 212, 185, 200, 215};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesOldExpected = {4, 3, 7, 7, 5, 4};
        int[] boardIndicesOldExpected = {182, 197, 212, 185, 200, 215};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("RAIN"));
    }

    @Test
    public void newWordIsPlacedVerticallyOnLastColumn_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesGenerated = {4, 1, 5, 5, 4, 3};
        int[] boardIndicesGenerated = {132, 133, 134, 177, 178, 179};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'A', 'I', 'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesUser = {1, 2, 4, 1, 5, 5, 4, 3};
        int[] boardIndicesUser = {149, 164, 132, 133, 134, 177, 178, 179};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'A', 'I', 'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesExpected = {1, 2, 4, 1, 5, 5, 4, 3};
        int[] boardIndicesExpected = {149, 164, 132, 133, 134, 177, 178, 179};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'R', 'S', 'U', 'N'};
        int[] valuesOldExpected = {4, 1, 5, 5, 4, 3};
        int[] boardIndicesOldExpected = {132, 133, 134, 177, 178, 179};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("RAIN"));
    }

    @Test
    public void newWordIsPlacedVerticallyOnLastColumn2_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'S', 'U', 'N'};
        int[] valuesGenerated = {5, 4, 3};
        int[] boardIndicesGenerated = {57, 58, 59};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'R', 'A', 'I', 'S', 'U', 'N'};
        int[] valuesUser = {5, 1, 3, 5, 4, 3};
        int[] boardIndicesUser = {14, 29,44, 57, 58, 59};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'R', 'A', 'I', 'S', 'U', 'N'};
        int[] valuesExpected = {5, 1, 3, 5, 4, 3};
        int[] boardIndicesExpected = {14, 29,44, 57, 58, 59};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'S', 'U', 'N'};
        int[] valuesOldExpected = {5, 4, 3};
        int[] boardIndicesOldExpected = {57, 58, 59};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("RAIN"));
    }

    @Test
    public void newWordIsPlacedVerticallyOnLastColumn3_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'R'};
        int[] valuesGenerated = {3, 1, 5};
        int[] boardIndicesGenerated = {178, 193, 208};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'R', 'C', 'A', 'T'};
        int[] valuesUser = {3, 1, 5, 3, 1, 6};
        int[] boardIndicesUser = {178, 193, 208, 194, 209, 224};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'R', 'C', 'A', 'T'};
        int[] valuesExpected = {3, 1, 5, 3, 1, 6};
        int[] boardIndicesExpected = {178, 193, 208, 194, 209, 224};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'A', 'R'};
        int[] valuesOldExpected = {3, 1, 5};
        int[] boardIndicesOldExpected = {178, 193, 208};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(3, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("CAT"));
        assertTrue(testGame.getWordsToBeContested().contains("AC"));
        assertTrue(testGame.getWordsToBeContested().contains("RA"));
    }

    @Test
    public void singleLetterIsPlacedOnPlayfield_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'O', 'M', 'P', 'L', 'E', 'X'};
        int[] valuesGenerated = {4, 5, 3, 3, 3, 1, 8};
        int[] boardIndicesGenerated = {112, 113, 114, 115, 116, 117, 118};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);
        testGame.setOldPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'O', 'M', 'P', 'L', 'E', 'X', 'A'};
        int[] valuesUser = {4, 5, 3, 3, 3, 1, 8, 1};
        int[] boardIndicesUser = {112, 113, 114, 115, 116, 117, 118, 119};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'O', 'M', 'P', 'L', 'E', 'X', 'A'};
        int[] valuesExpected = {4, 5, 3, 3, 3, 1, 8, 1};
        int[] boardIndicesExpected = {112, 113, 114, 115, 116, 117, 118, 119};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        char[] lettersOldExpected = {'C', 'O', 'M', 'P', 'L', 'E', 'X'};
        int[] valuesOldExpected = {4, 5, 3, 3, 3, 1, 8};
        int[] boardIndicesOldExpected = {112, 113, 114, 115, 116, 117, 118, 119};
        List<Tile> expectedOldPlayfield = fillBoard(lettersOldExpected, valuesOldExpected, boardIndicesOldExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertArrayEquals(expectedOldPlayfield.toArray(), testGame.getOldPlayfield().toArray());
        assertEquals(1, testGame.getWordsToBeContested().size());
        assertTrue(testGame.getWordsToBeContested().contains("COMPLEXA"));
    }

    @Test
    public void firstWord_NotPlacedInCenterOfPlayfield_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T'};
        int[] valuesUser = {4, 3, 5};
        int[] boardIndicesUser = {113, 128, 143};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void firstWordPlaced_onlyOneLetter_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }

        // sent Playfield by user
        char[] letterUser = {'C'};
        int[] valueUser = {4};
        int[] boardIndexUser = {112};
        List<Tile> userPlayfield = fillBoard(letterUser, valueUser, boardIndexUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedHorizontally_notConnectedToExistingTile_throwError() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 127, 142};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 144, 145};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedHorizontally_newTilesNotConnected_throwError() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 127, 142};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 141, 144};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedVertically_notConnectedToExistingWord_throwError() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 127, 142};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 144, 159};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedVertically_newTilesNotConnected_throwError() {
        // given
        // saved Playfield in database
        char[] letterGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 127, 142};
        List<Tile> generatedPlayfield = fillBoard(letterGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 143, 173};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordIsPlaced_NotHorizontallyNorVertically_throwError() {
        // given
        // saved Playfield in database
        char[] letterGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 127, 142};
        List<Tile> generatedPlayfield = fillBoard(letterGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'T'};
        int[] valuesUser = {4, 3, 5, 4, 3, 5};
        int[] boardIndicesUser = {112, 127, 142, 143, 159, 173};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordIsPlaced_NotHorizontallyNorVertically2_throwError() {
        // given
        // saved Playfield in database
        char[] letterGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 113, 114};
        List<Tile> generatedPlayfield = fillBoard(letterGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'T'};
        int[] valuesUser = {4, 3, 5, 4, 3, 5};
        int[] boardIndicesUser = {112, 113, 114, 129, 145, 161};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordIsPlaced_existingTileIsOverwritten_throwError() {
        // given
        // saved Playfield in database
        char[] letterGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 113, 114};
        List<Tile> generatedPlayfield = fillBoard(letterGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'C', 'A', 'T'};
        int[] valuesUser = {4, 4, 3, 5};
        int[] boardIndicesUser = {112, 113, 114, 115};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userPlayfield.get(113).setId(200L);
        userPlayfield.get(114).setId(201L);
        userPlayfield.get(115).setId(202L);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void singleTileIsPlaced_NotConnectedToExistingTile_throwError() {
        // given
        // saved Playfield in database
        char[] letterGenerated = {'C', 'A', 'T'};
        int[] valuesGenerated = {4, 3, 5};
        int[] boardIndicesGenerated = {112, 113, 114};
        List<Tile> generatedPlayfield = fillBoard(letterGenerated, valuesGenerated, boardIndicesGenerated);

        // sent Playfield by user
        char[] lettersUser = {'C', 'A', 'T', 'C'};
        int[] valuesUser = {4, 3, 5, 4};
        int[] boardIndicesUser = {112, 113, 114, 224};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void swapTiles_incorrectId_throwError() {
        // given
        Hand returnedHand = new Hand();
        returnedHand.setId(1L);
        returnedHand.setHanduserid(2L);

        List<Tile> handTiles = new ArrayList<>();
        Game returnedGame = new Game();

        given(handRepository.findById(Mockito.any())).willReturn(Optional.of(returnedHand));
        given(gameRepository.findById(Mockito.any())).willReturn(Optional.of(returnedGame));

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.swapTiles(1L, 1L, 1L, handTiles));
    }

    @Test
    public void swapTiles_tooManyTilesToBeSwapped_throwError() {
        // given
        Hand returnedHand = new Hand();
        returnedHand.setId(1L);
        returnedHand.setHanduserid(1L);

        char[] letters = {'A', 'C', 'J', 'K', 'Q', 'A', 'E', 'E'};
        int[] values = {3, 4, 6, 6, 7, 3, 2, 2};
        List<Tile> handTiles = fillHandOrBag(letters, values);

        Game returnedGame = new Game();

        given(handRepository.findById(Mockito.any())).willReturn(Optional.of(returnedHand));
        given(gameRepository.findById(Mockito.any())).willReturn(Optional.of(returnedGame));

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.swapTiles(1L, 1L, 1L, handTiles));
    }

    @Test
    public void swapTiles_zeroTilesToBeSwapped_throwError() {
        // given
        Hand returnedHand = new Hand();
        returnedHand.setId(1L);
        returnedHand.setHanduserid(1L);

        List<Tile> handTiles = new ArrayList<>();

        Game returnedGame = new Game();

        given(handRepository.findById(Mockito.any())).willReturn(Optional.of(returnedHand));
        given(gameRepository.findById(Mockito.any())).willReturn(Optional.of(returnedGame));

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.swapTiles(1L, 1L, 1L, handTiles));
    }

    @Test
    public void swapTiles_invalidTile_throwError() {
        // given
        Hand returnedHand = new Hand();
        returnedHand.setId(1L);
        returnedHand.setHanduserid(1L);

        // saved hand in database
        char[] lettersDatabase = {'A', 'C', 'J', 'K', 'Q', 'A', 'E'};
        int[] valuesDatabase = {3, 4, 6, 6, 7, 3, 2};
        List<Tile> handTiles = fillHandOrBag(lettersDatabase, valuesDatabase);
        for (int i = 0; i < handTiles.size(); i++) {
            handTiles.get(i).setId((long) i + 1);
        }
        returnedHand.setHandtiles(handTiles);

        // tiles to be swapped
        char[] letters_tilesToBeSwapped = {'A', 'C', 'Z'};
        int[] values_tilesToBeSwapped = {3, 4, 10};
        List<Tile> tilesToBeSwapped = fillHandOrBag(letters_tilesToBeSwapped, values_tilesToBeSwapped);
        tilesToBeSwapped.get(0).setId(1L);
        tilesToBeSwapped.get(1).setId(2L);
        tilesToBeSwapped.get(2).setId(12L);

        Game returnedGame = new Game();

        given(handRepository.findById(Mockito.any())).willReturn(Optional.of(returnedHand));
        given(gameRepository.findById(Mockito.any())).willReturn(Optional.of(returnedGame));

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.swapTiles(1L, 1L, 1L, tilesToBeSwapped));
    }

    @Test
    public void swapTiles_notEnoughTilesInBag_throwError() {
        // given
        Hand returnedHand = new Hand();
        returnedHand.setId(1L);
        returnedHand.setHanduserid(1L);

        // saved hand in database
        char[] lettersDatabase = {'A', 'C', 'J', 'K', 'Q', 'A', 'E'};
        int[] valuesDatabase = {3, 4, 6, 6, 7, 3, 2};
        List<Tile> handTiles = fillHandOrBag(lettersDatabase, valuesDatabase);
        returnedHand.setHandtiles(handTiles);

        // tiles to be swapped
        char[] letters_tilesToBeSwapped = {'A', 'C', 'J'};
        int[] values_tilesToBeSwapped = {3, 4, 6};
        List<Tile> tilesToBeSwapped = fillHandOrBag(letters_tilesToBeSwapped, values_tilesToBeSwapped);

        Game returnedGame = new Game();
        Bag returnedBag = new Bag();
        returnedBag.setId(1L);

        // tiles in Bag
        char[] lettersInBag = {'Q', 'T'};
        int[] valuesInBag = {7, 6};
        List<Tile> tilesInBag = fillHandOrBag(lettersInBag, valuesInBag);
        returnedBag.setTiles(tilesInBag);
        returnedGame.setBag(returnedBag);

        given(handRepository.findById(Mockito.any())).willReturn(Optional.of(returnedHand));
        given(gameRepository.findById(Mockito.any())).willReturn(Optional.of(returnedGame));
        given(bag.tilesleft()).willReturn(2);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.swapTiles(1L, 1L, 1L, tilesToBeSwapped));
    }

    @Test
    public void swapTiles_validSwap_returnNewHand() {
        // given
        Hand returnedHand = new Hand();
        returnedHand.setId(1L);
        returnedHand.setHanduserid(1L);

        // saved hand in database
        char[] lettersDatabase = {'A', 'C', 'J', 'K', 'Q', 'A', 'E'};
        int[] valuesDatabase = {3, 4, 6, 6, 7, 3, 2};
        List<Tile> handTiles = fillHandOrBag(lettersDatabase, valuesDatabase);
        for (int i = 0; i < handTiles.size(); i++) {
            handTiles.get(i).setId((long) i + 1);
        }
        returnedHand.setHandtiles(handTiles);

        // tiles to be swapped
        char[] letters_tilesToBeSwapped = {'A', 'C', 'Q'};
        int[] values_tilesToBeSwapped = {3, 4, 7};
        List<Tile> tilesToBeSwapped = fillHandOrBag(letters_tilesToBeSwapped, values_tilesToBeSwapped);
        tilesToBeSwapped.get(0).setId(1L);
        tilesToBeSwapped.get(1).setId(2L);
        tilesToBeSwapped.get(2).setId(5L);

        // tiles in Bag
        Game returnedGame = new Game();
        Bag returnedBag = new Bag();
        returnedBag.setId(1L);

        char[] lettersInBag = {'Q', 'T', 'M'};
        int[] valuesInBag = {7, 6, 4};
        List<Tile> tilesInBag = fillHandOrBag(lettersInBag, valuesInBag);
        tilesInBag.get(0).setId(12L);
        tilesInBag.get(1).setId(13L);
        tilesInBag.get(2).setId(14L);
        returnedBag.setTiles(tilesInBag);
        returnedGame.setBag(returnedBag);
        returnedGame.setPlayers(testUsers);
        returnedGame.setCurrentPlayer(1L);

        given(handRepository.findById(Mockito.any())).willReturn(Optional.of(returnedHand));
        given(gameRepository.findById(Mockito.any())).willReturn(Optional.of(returnedGame));
        when(bagRepository.findById(Mockito.any())).thenReturn(Optional.of(returnedBag));
        //given(bag.tilesleft()).willReturn(3);
        //given(bag.getSomeTiles(3)).willReturn(tilesInBag);

        // when/then
        List<Tile> newHand = gameService.swapTiles(1L, 1L, 1L, tilesToBeSwapped);
        List<Character> newLetters = new ArrayList<>();
        for (int i = 0; i < newHand.size(); i++){
            newLetters.add(i, newHand.get(i).getLetter());
        }
        List<Character> newBag = new ArrayList<>();
        for (int i = 0; i < returnedBag.getTiles().size(); i++){
            newBag.add(i, returnedBag.getTiles().get(i).getLetter());
        }

        assertEquals(7, newHand.size());
        assertTrue(newLetters.contains('J'));
        assertTrue(newLetters.contains('K'));
        assertTrue(newLetters.contains('A'));
        assertTrue(newLetters.contains('E'));
        assertTrue(newLetters.contains('Q'));
        assertTrue(newLetters.contains('T'));
        assertTrue(newLetters.contains('M'));
        assertEquals(3, returnedGame.getBag().tilesleft());
        assertTrue(newBag.contains('A'));
        assertTrue(newBag.contains('C'));
        assertTrue(newBag.contains('Q'));
    }

    @Test
    public void contestWord_validContest_allPlayersContested_contestSuccessful() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Game game = new Game();
        game.setId(3L);
        game.setGameRound(1L);

        Bag bag = new Bag();
        bag.setId(4L);

        List<Tile> tilesInBag = new ArrayList<>();
        tilesInBag.add(new Tile('A', 2));
        tilesInBag.add(new Tile('C', 3));
        bag.setTiles(tilesInBag);

        Hand hand1 = new Hand();
        hand1.setId(5L);
        List<Tile> tilesInHand1 = new ArrayList<>();
        tilesInHand1.add(new Tile('A', 2));
        tilesInHand1.add(new Tile('C', 3));
        tilesInHand1.add(new Tile('A', 2));
        tilesInHand1.add(new Tile('Z', 10));
        tilesInHand1.add(new Tile('K', 4));
        tilesInHand1.add(new Tile('T', 5));
        tilesInHand1.add(new Tile('Q', 10));

        Hand hand2 = new Hand();
        hand2.setId(6L);
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
        game.setWordContested(false);

        List<Tile> oldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            oldPlayfield.add(null);
        }
        game.setOldPlayfield(oldPlayfield);

        List<Tile> currentPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            currentPlayfield.add(null);
        }
        Tile tile1 = new Tile('Q', 10);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('O', 6);
        tile2.setBoardidx(113);
        currentPlayfield.set(112, tile1);
        currentPlayfield.set(113, tile2);
        game.setPlayfield(currentPlayfield);

        Score score1 = new Score();
        score1.setId(10L);
        score1.setScoreUserId(1L);
        score1.setScore(0);
        Score score2 = new Score();
        score2.setId(11L);
        score2.setScoreUserId(2L);
        score2.setScore(0);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);

        // when
        gameService.contestWord(game.getId(), user2, true);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedPlayfield.add(null);
        }

        assertEquals(2L, game.getCurrentPlayer());
        assertTrue(game.getWordContested());
        assertArrayEquals(expectedPlayfield.toArray(), game.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), game.getPlayfield().toArray());
    }

    @Test
    public void contestWord_validContest_allPlayersContested_contestSuccessful2() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Game game = new Game();
        game.setId(3L);
        game.setGameRound(2L);

        Bag bag = new Bag();
        bag.setId(4L);

        List<Tile> tilesInBag = new ArrayList<>();
        tilesInBag.add(new Tile('A', 2));
        tilesInBag.add(new Tile('C', 3));
        bag.setTiles(tilesInBag);

        Hand hand1 = new Hand();
        hand1.setId(5L);
        List<Tile> tilesInHand1 = new ArrayList<>();
        tilesInHand1.add(new Tile('A', 2));
        tilesInHand1.add(new Tile('C', 3));
        tilesInHand1.add(new Tile('A', 2));
        tilesInHand1.add(new Tile('Z', 10));
        tilesInHand1.add(new Tile('K', 4));
        tilesInHand1.add(new Tile('T', 5));
        tilesInHand1.add(new Tile('Q', 10));

        Hand hand2 = new Hand();
        hand2.setId(6L);
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

        List<Tile> oldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            oldPlayfield.add(null);
        }
        Tile tile1 = new Tile('C', 3);
        tile1.setId(100L);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('A', 2);
        tile2.setId(101L);
        tile2.setBoardidx(113);
        Tile tile3 = new Tile('R', 5);
        tile3.setId(102L);
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
        tile4.setId(200L);
        tile4.setBoardidx(84);
        Tile tile5 = new Tile('O', 6);
        tile5.setId(201L);
        tile5.setBoardidx(99);

        currentPlayfield.set(84, tile4);
        currentPlayfield.set(99, tile5);
        currentPlayfield.set(112, tile1);
        currentPlayfield.set(113, tile2);
        currentPlayfield.set(114, tile3);
        game.setPlayfield(currentPlayfield);

        Score score1 = new Score();
        score1.setId(10L);
        score1.setScoreUserId(1L);
        score1.setScore(0);
        Score score2 = new Score();
        score2.setId(11L);
        score2.setScoreUserId(2L);
        score2.setScore(10);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);
        game.setWordContested(false);

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);

        // when
        gameService.contestWord(game.getId(), user2, true);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedPlayfield.add(null);
        }

        expectedPlayfield.set(112, tile1);
        expectedPlayfield.set(113, tile2);
        expectedPlayfield.set(114, tile3);

        assertEquals(2L, game.getCurrentPlayer());
        assertTrue(game.getWordContested());
        assertArrayEquals(expectedPlayfield.toArray(), game.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), game.getPlayfield().toArray());
    }

    @Test
    public void contestWord_wordNotContested_oldPlayfieldOverwritten() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Game game = new Game();
        game.setId(3L);
        game.setGameRound(2L);

        Bag bag = new Bag();
        bag.setId(4L);

        List<Tile> tilesInBag = new ArrayList<>();
        tilesInBag.add(new Tile('A', 2));
        tilesInBag.add(new Tile('C', 3));
        bag.setTiles(tilesInBag);

        Hand hand1 = new Hand();
        hand1.setId(5L);
        List<Tile> tilesInHand1 = new ArrayList<>();
        tilesInHand1.add(new Tile('Q', 10));
        tilesInHand1.add(new Tile('O', 6));
        tilesInHand1.get(0).setId(200L);
        tilesInHand1.get(1).setId(201L);
        hand1.setHandtiles(tilesInHand1);
        hand1.setHanduserid(1L);

        Hand hand2 = new Hand();
        hand2.setId(6L);
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

        List<Tile> oldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            oldPlayfield.add(null);
        }
        Tile tile1 = new Tile('C', 3);
        tile1.setId(100L);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('A', 2);
        tile2.setId(101L);
        tile2.setBoardidx(113);
        Tile tile3 = new Tile('R', 5);
        tile3.setId(102L);
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
        tile4.setId(200L);
        tile4.setBoardidx(84);
        Tile tile5 = new Tile('O', 6);
        tile5.setId(201L);
        tile5.setBoardidx(99);

        currentPlayfield.set(84, tile4);
        currentPlayfield.set(99, tile5);
        currentPlayfield.set(112, tile1);
        currentPlayfield.set(113, tile2);
        currentPlayfield.set(114, tile3);
        game.setPlayfield(currentPlayfield);

        Score score1 = new Score();
        score1.setId(10L);
        score1.setScoreUserId(1L);
        score1.setScore(0);
        Score score2 = new Score();
        score2.setId(11L);
        score2.setScoreUserId(2L);
        score2.setScore(10);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);
        game.setWordContested(false);

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));
        given(handRepository.findByHanduserid(game.getCurrentPlayer())).willReturn(hand1);
        given(scoreRepository.findByScoreUserId(game.getCurrentPlayer())).willReturn(score1);

        // when
        gameService.contestWord(game.getId(), user2, false);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedPlayfield.add(null);
        }

        expectedPlayfield.set(112, tile1);
        expectedPlayfield.set(113, tile2);
        expectedPlayfield.set(114, tile3);
        expectedPlayfield.set(84, tile4);
        expectedPlayfield.set(99, tile5);

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

    @Test
    public void contestWord_validContest_allPlayersContested_ContestUnsuccessful_oldPlayfieldOverwritten() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Game game = new Game();
        game.setId(3L);
        game.setGameRound(2L);

        Bag bag = new Bag();
        bag.setId(4L);

        List<Tile> tilesInBag = new ArrayList<>();
        tilesInBag.add(new Tile('A', 2));
        tilesInBag.add(new Tile('C', 3));
        bag.setTiles(tilesInBag);

        Hand hand1 = new Hand();
        hand1.setId(5L);
        List<Tile> tilesInHand1 = new ArrayList<>();
        tilesInHand1.add(new Tile('Q', 10));
        tilesInHand1.add(new Tile('O', 6));
        tilesInHand1.get(0).setId(200L);
        tilesInHand1.get(1).setId(201L);
        hand1.setHandtiles(tilesInHand1);
        hand1.setHanduserid(1L);

        Hand hand2 = new Hand();
        hand2.setId(6L);
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
        game.setWordContested(false);

        List<Tile> oldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            oldPlayfield.add(null);
        }
        Tile tile1 = new Tile('C', 3);
        tile1.setId(100L);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('A', 2);
        tile2.setId(101L);
        tile2.setBoardidx(113);
        Tile tile3 = new Tile('R', 5);
        tile3.setId(102L);
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
        tile4.setId(200L);
        tile4.setBoardidx(84);
        Tile tile5 = new Tile('O', 6);
        tile5.setId(201L);
        tile5.setBoardidx(99);

        currentPlayfield.set(84, tile4);
        currentPlayfield.set(99, tile5);
        currentPlayfield.set(112, tile1);
        currentPlayfield.set(113, tile2);
        currentPlayfield.set(114, tile3);
        game.setPlayfield(currentPlayfield);

        Score score1 = new Score();
        score1.setId(10L);
        score1.setScoreUserId(1L);
        score1.setScore(0);
        Score score2 = new Score();
        score2.setId(11L);
        score2.setScoreUserId(2L);
        score2.setScore(10);

        List<Score> scores = new ArrayList<>();
        scores.add(score1);
        scores.add(score2);
        game.setScores(scores);

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));
        given(handRepository.findByHanduserid(game.getCurrentPlayer())).willReturn(hand1);

        // when
        gameService.contestWord(game.getId(), user2, true);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedPlayfield.add(null);
        }

        expectedPlayfield.set(112, tile1);
        expectedPlayfield.set(113, tile2);
        expectedPlayfield.set(114, tile3);
        expectedPlayfield.set(84, tile4);
        expectedPlayfield.set(99, tile5);

        List<Character> lettersInHand1 = new ArrayList<>();
        for (int i = 0; i < 2; i++){
            lettersInHand1.add(game.getHands().get(0).getHandtiles().get(i).getLetter());
        }

        assertEquals(2L, game.getCurrentPlayer());
        assertTrue(game.getWordContested());
        assertArrayEquals(expectedPlayfield.toArray(), game.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), game.getPlayfield().toArray());
        assertEquals(0, game.getBag().getTiles().size());
        assertEquals(2, game.getHands().get(0).getHandtiles().size());
        assertEquals(7, game.getHands().get(1).getHandtiles().size());
        assertTrue(lettersInHand1.contains('A'));
        assertTrue(lettersInHand1.contains('C'));
    }

    @Test
    public void contestWord_validContest_notAllPlayersContestedYet_nothingUpdated() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(20L);

        Game game = new Game();
        game.setId(3L);
        game.setWordContested(false);
        game.setCurrentPlayer(1L);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);
        game.setPlayers(players);

        List<Tile> oldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            oldPlayfield.add(null);
        }
        Tile tile1 = new Tile('C', 3);
        tile1.setId(100L);
        tile1.setBoardidx(112);
        Tile tile2 = new Tile('A', 2);
        tile2.setId(101L);
        tile2.setBoardidx(113);
        Tile tile3 = new Tile('R', 5);
        tile3.setId(102L);
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
        tile4.setId(200L);
        tile4.setBoardidx(84);
        Tile tile5 = new Tile('O', 6);
        tile5.setId(201L);
        tile5.setBoardidx(99);

        currentPlayfield.set(84, tile4);
        currentPlayfield.set(99, tile5);
        currentPlayfield.set(112, tile1);
        currentPlayfield.set(113, tile2);
        currentPlayfield.set(114, tile3);
        game.setPlayfield(currentPlayfield);

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));

        // when
        gameService.contestWord(game.getId(), user2, true);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedPlayfield.add(null);
        }

        expectedPlayfield.set(112, tile1);
        expectedPlayfield.set(113, tile2);
        expectedPlayfield.set(114, tile3);
        expectedPlayfield.set(84, tile4);
        expectedPlayfield.set(99, tile5);

        List<Tile> expectedoldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedoldPlayfield.add(null);
        }

        expectedoldPlayfield.set(112, tile1);
        expectedoldPlayfield.set(113, tile2);
        expectedoldPlayfield.set(114, tile3);

        assertEquals(1L, game.getCurrentPlayer());
        assertFalse(game.getWordContested());
        assertArrayEquals(expectedoldPlayfield.toArray(), game.getOldPlayfield().toArray());
        assertArrayEquals(expectedPlayfield.toArray(), game.getPlayfield().toArray());
    }

    @Test
    public void contestWord_playerContestsMoreThanOnce_invalidContest_throwError() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(20L);

        Game game = new Game();
        game.setId(3L);
        game.setWordContested(true);
        game.setCurrentPlayer(1L);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);
        game.setPlayers(players);

        Map<Long, Boolean> decisionsContesting = new HashMap<>();
        decisionsContesting.put(user2.getId(), true);
        game.setDecisionPlayersContestation(decisionsContesting);

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.contestWord(game.getId(), user2, true));
    }

    @Test
    public void contestWord_playerWhoPlacedWordWantsToContest_invalidContest_throwError() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        User user3 = new User();
        user3.setId(20L);

        Game game = new Game();
        game.setId(3L);
        game.setWordContested(true);
        game.setCurrentPlayer(1L);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);
        game.setPlayers(players);

        Map<Long, Boolean> decisionsContesting = new HashMap<>();
        decisionsContesting.put(user2.getId(), true);
        game.setDecisionPlayersContestation(decisionsContesting);

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.contestWord(game.getId(), user1, true));
    }

    private List<Tile> fillBoard(char[] letters, int[] values, int[] boardIndices){
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }

        for (int i = 0; i < letters.length; i++){
            Tile tile = new Tile(letters[i], values[i]);
            tile.setBoardidx(boardIndices[i]);
            tile.setId((long) boardIndices[i]);
            generatedPlayfield.set(tile.getBoardidx(), tile);
        }

        return generatedPlayfield;
    }
    private List<Tile> fillHandOrBag(char[] letters, int[] values){
        List<Tile> listToBeFilled = new ArrayList<>();
        for (int i = 0; i < letters.length; i++){
            Tile tile = new Tile(letters[i], values[i]);
            listToBeFilled.add(tile);
        }
        return listToBeFilled;
    }
}
