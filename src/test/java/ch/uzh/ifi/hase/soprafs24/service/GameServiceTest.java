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

        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);

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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
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
        game.setWordContested(true);

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
        assertFalse(game.getWordContested());
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
        when(dictionary.getScrabbleScore(Mockito.anyString())).thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(404);

        // when
        gameService.contestWord(game.getId(), user2, true);

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
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Game game = new Game();
        game.setId(3L);

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
        given(scoreRepository.findByScoreUserId(game.getCurrentPlayer())).willReturn(score1);

        // when
        gameService.contestWord(game.getId(), user2, false);

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

    @Test
    public void contestWord_validContest_allPlayersContested_ContestUnsuccessful_oldPlayfieldOverwritten() {
        // given
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        Game game = new Game();
        game.setId(3L);

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
        game.setWordContested(true);
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

        given(gameRepository.findById(game.getId())).willReturn(Optional.of(game));

        // when
        gameService.contestWord(game.getId(), user2, true);

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

        List<Tile> expectedoldPlayfield = new ArrayList<>();
        for (int i = 0; i<225; i++){
            expectedoldPlayfield.add(null);
        }

        Tile tile14 = new Tile('C', 3);
        tile14.setBoardidx(112);
        Tile tile15 = new Tile('A', 2);
        tile15.setBoardidx(113);
        Tile tile16 = new Tile('R', 5);
        tile16.setBoardidx(114);
        expectedoldPlayfield.set(112, tile14);
        expectedoldPlayfield.set(113, tile15);
        expectedoldPlayfield.set(114, tile16);

        assertEquals(1L, game.getCurrentPlayer());
        assertTrue(game.getWordContested());
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
