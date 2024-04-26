package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.dictionary.Dictionary;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.BagRepository;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.HandRepository;
import ch.uzh.ifi.hase.soprafs24.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

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

        Mockito.when(scoreRepository.findByScoreUserId(Mockito.any())).thenReturn(testScore1);
        Mockito.when(bagRepository.findById(Mockito.any())).thenReturn(Optional.of(bag));
        Mockito.when(handRepository.findByHanduserid(Mockito.any())).thenReturn(testHand1);

        Mockito.when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
    }

    @Test
    public void skipTurn_2Players_success() {
        User user = testGame.getPlayers().get(0);
        Mockito.when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        gameService.skipTurn(user, 1L);
        assertEquals(2L, testGame.getCurrentPlayer());
    }

    @Test
    public void nextPlayer_2Players_success() {
        Mockito.when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        gameService.makeNextPlayerToCurrentPlayer(1L);
        assertEquals(2L, testGame.getCurrentPlayer());
    }

    @Test
    public void nextPlayer_3Players_success() {
        User testUser3 = new User();
        testUser3.setId(3L);
        testGame.getPlayers().add(testUser3);
        testGame.setCurrentPlayer(3L);

        Mockito.when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        gameService.makeNextPlayerToCurrentPlayer(1L);
        assertEquals(1L, testGame.getCurrentPlayer());
    }

    @Test
    public void nextPlayer_wrongId_fail() {
        Mockito.when(gameRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class ,() -> gameService.makeNextPlayerToCurrentPlayer(2L));
    }

    @Test
    public void authenticatePlayer_invalidGameId_fail() {
        User user = testGame.getPlayers().get(0);

        Mockito.when(gameRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class ,() -> gameService.authenticateUserForMove(user, 2L));
    }

    @Test
    public void authenticatePlayer_invalidUserId_fail() {
        User user = testGame.getPlayers().get(1);

        Mockito.when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));
        assertThrows(ResponseStatusException.class ,() -> gameService.authenticateUserForMove(user, 1L));
    }

    @Test
    public void authenticatePlayer_success() {
        User user = testGame.getPlayers().get(0);

        Mockito.when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        assertDoesNotThrow(() -> gameService.authenticateUserForMove(user, 1L));
    }

    @Test
    public void validateWord_isValid() {
        Mockito.when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        Mockito.when(mockResponse.statusCode()).thenReturn(200);

        assertTrue(gameService.validateWord("testWord"));
    }

    @Test
    public void validateWord_isInvalid() {
        Mockito.when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        Mockito.when(mockResponse.statusCode()).thenReturn(404);

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
        Mockito.when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        Mockito.when(mockResponse.statusCode()).thenReturn(200);
        Mockito.when(mockResponse.body()).thenReturn("{" + '"' + "value" + '"' + ":8}");

        assertEquals(8, gameService.getScrabbleScore("testWord"));
    }

    @Test
    public void getScrabbleScore_wordIsInvalid() {
        Mockito.when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        Mockito.when(mockResponse.statusCode()).thenReturn(404);

        assertEquals(0, gameService.getScrabbleScore("testWord"));
    }

    @Test
    public void getScrabbleScore_throwsException() {
        ResponseStatusException response = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        doThrow(response).when(dictionary).getScrabbleScore(Mockito.any());

        assertThrows(ResponseStatusException.class, () -> gameService.getScrabbleScore("testWord"));
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

        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.of(testGame));
        Mockito.when(mockResponse.statusCode()).thenReturn(404);


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

        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.of(testGame));

        gameService.changeScoresAfterContesting(testGame, contested, words);

        assertEquals(70, testGame.getScores().get(0).getScore());
        assertEquals(30, testGame.getScores().get(1).getScore());
    }

    @Test
    public void allTilesPlayed_validMove_saveUpdatedPlayfield() {
        List<Tile> generatedPlayfield = new ArrayList<>();

        for (int i = 0; i < 225; i++) {
            generatedPlayfield.add(i, null);
        }

        testGame.setPlayfield(generatedPlayfield);

        Game userGame = new Game();
        char[] lettersUser = {'A', 'A', 'A', 'A', 'A', 'A', 'A'};
        int[] valuesUser = {3, 3, 3, 3, 3, 3, 3};
        int[] boardIndicesUser = {109, 110, 111, 112, 113, 114, 115};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        char[] lettersExpected = {'A', 'A', 'A', 'A', 'A', 'A', 'A'};
        int[] valuesExpected = {3, 3, 3, 3, 3, 3, 3};
        int[] boardIndicesExpected = {109, 110, 111, 112, 113, 114, 115};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals(7 * 3 * 2 + 50, testScore1.getScore());
    }

    @Test
    public void wordPlayed_invalidWord_saveOriginalPlayfield() {
        List<Tile> generatedPlayfield = new ArrayList<>();
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
            expectedPlayfield.add(i, null);
        }
        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'A', 'A', 'A'};
        int[] valuesUser = {4, 3, 5};
        int[] boardIndicesUser = {112, 127, 142};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        Mockito.when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        Mockito.when(mockResponse.statusCode()).thenReturn(404);

        gameService.placeTilesOnBoard(userGame);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals(0, testScore1.getScore());
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

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T'};
        int[] valuesUser = {4, 3, 5};
        int[] boardIndicesUser = {112, 127, 142};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T'};
        int[] valuesExpected = {4, 3, 5};
        int[] boardIndicesExpected = {112, 127, 142};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals((4 + 3 + 5) * 2, testScore1.getScore());
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

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 143, 144};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesExpected = {4, 3, 5, 2, 3};
        int[] boardIndicesExpected = {112, 127, 142, 143, 144};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals(5 + 2 + 3 * 3, testScore1.getScore());
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

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesUser = {4, 3, 5, 2, 3};
        int[] boardIndicesUser = {112, 127, 142, 126, 128};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'E', 'A'};
        int[] valuesExpected = {4, 3, 5, 2, 3};
        int[] boardIndicesExpected = {112, 127, 142, 126, 128};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals(2 * 2 + 3 + 3 * 2, testScore1.getScore());
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

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'D', 'O', 'G'};
        int[] valuesUser = {4, 3, 5, 2, 6, 4};
        int[] boardIndicesUser = {112, 113, 114, 128, 129, 130};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'D', 'O', 'G'};
        int[] valuesExpected = {4, 3, 5, 2, 6, 4};
        int[] boardIndicesExpected = {112, 113, 114, 128, 129, 130};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals((3 + 2 * 2) + (5 + 6) + (2 * 2 + 6 + 4), testScore1.getScore());
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

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A'};
        int[] valuesUser = {4, 3, 5, 4, 3};
        int[] boardIndicesUser = {112, 113, 114, 82, 97};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A'};
        int[] valuesExpected = {4, 3, 5, 4, 3};
        int[] boardIndicesExpected = {112, 113, 114, 82, 97};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals(4 + 3 + 4, testScore1.getScore());
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

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 7};
        int[] boardIndicesUser = {111, 112, 113, 97, 127};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 7};
        int[] boardIndicesExpected = {111, 112, 113, 97, 127};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals(4 + 3 + 7, testScore1.getScore());
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

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesUser = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesUser = {97, 112, 127, 113, 128, 143};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'C', 'A', 'R'};
        int[] valuesExpected = {4, 3, 5, 4, 3, 7};
        int[] boardIndicesExpected = {97, 112, 127, 113, 128, 143};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void singleLetterIsPlacedOnPlayfield_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        char[] lettersGenerated = {'C', 'A', 'T', 'T', 'T', 'T', 'T'};
        int[] valuesGenerated = {4, 3, 5, 5, 5, 5, 5};
        int[] boardIndicesGenerated = {112, 113, 114, 115, 116, 117, 118};
        List<Tile> generatedPlayfield = fillBoard(lettersGenerated, valuesGenerated, boardIndicesGenerated);
        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        char[] lettersUser = {'C', 'A', 'T', 'T', 'T', 'T', 'T', 'T'};
        int[] valuesUser = {4, 3, 5, 5, 5, 5, 5, 5};
        int[] boardIndicesUser = {112, 113, 114, 115, 116, 117, 118, 119};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        char[] lettersExpected = {'C', 'A', 'T', 'T', 'T', 'T', 'T', 'T'};
        int[] valuesExpected = {4, 3, 5, 5, 5, 5, 5, 5};
        int[] boardIndicesExpected = {112, 113, 114, 115, 116, 117, 118, 119};
        List<Tile> expectedPlayfield = fillBoard(lettersExpected, valuesExpected, boardIndicesExpected);
        
        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
        assertEquals(4 + 3 + 5 + 5 + 5 + 5 + 5 + 3 * 5, testScore1.getScore());
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
        char[] lettersUser = {'C', 'A', 'T', 'C', 'A', 'T'};
        int[] valuesUser = {4, 3, 5, 4, 3, 5};
        int[] boardIndicesUser = {112, 113, 114, 113, 114, 115};
        List<Tile> userPlayfield = fillBoard(lettersUser, valuesUser, boardIndicesUser);

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
        Mockito.when(bagRepository.findById(Mockito.any())).thenReturn(Optional.of(returnedBag));
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

    private List<Tile> fillBoard(char[] letters, int[] values, int[] boardIndices){
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }

        for (int i = 0; i < letters.length; i++){
            Tile tile = new Tile(letters[i], values[i]);
            tile.setBoardidx(boardIndices[i]);
            tile.setId((long) i);
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
