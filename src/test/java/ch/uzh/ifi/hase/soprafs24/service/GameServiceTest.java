package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.dictionary.Dictionary;
import ch.uzh.ifi.hase.soprafs24.entity.*;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs24.repository.HandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private Bag bag;

    @Mock
    private HttpResponse<String> mockResponse;

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private List<User> testUsers = new ArrayList<>();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testGame = new Game();
        testGame.setId(1L);

        User testUser1 = new User();
        testUser1.setId(1L);
        User testUser2 = new User();
        testUser2.setId(2L);

        testUsers.add(testUser1);
        testUsers.add(testUser2);

        testGame.setPlayers(testUsers);
        testGame.setCurrentPlayer(1L);
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
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        userPlayfield.set(112, tile1);
        userPlayfield.set(127, tile2);
        userPlayfield.set(142, tile3);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);

        expectedPlayfield.set(112, tile4);
        expectedPlayfield.set(127, tile5);
        expectedPlayfield.set(142, tile6);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void wordIsPlacedHorizontallyOnPlayfield_validMove_saveUpdatedPlayfield() {
        /* In this test, a new word is placed horizontally to the right of
        an existing tile*/

        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(127, tile2);
        generatedPlayfield.set(142, tile3);

        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('E', 2);
        Tile tile8 = new Tile('A', 3);

        userPlayfield.set(112, tile4);
        userPlayfield.set(127, tile5);
        userPlayfield.set(142, tile6);
        userPlayfield.set(143, tile7);
        userPlayfield.set(144, tile8);

        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile9 = new Tile('C', 4);
        Tile tile10 = new Tile('A', 3);
        Tile tile11 = new Tile('T', 5);
        Tile tile12 = new Tile('E', 2);
        Tile tile13 = new Tile('A', 3);

        expectedPlayfield.set(112, tile9);
        expectedPlayfield.set(127, tile10);
        expectedPlayfield.set(142, tile11);
        expectedPlayfield.set(143, tile12);
        expectedPlayfield.set(144, tile13);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void wordIsPlacedHorizontallyOnPlayfield2_validMove_saveUpdatedPlayfield() {
        /* In this test, a new word is placed horizontally, whereby at least 2 new tiles
        are connected by an existing tile*/

        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(127, tile2);
        generatedPlayfield.set(142, tile3);
        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('E', 2);
        Tile tile8 = new Tile('A', 3);

        userPlayfield.set(112, tile4);
        userPlayfield.set(127, tile5);
        userPlayfield.set(142, tile6);
        userPlayfield.set(126, tile7);
        userPlayfield.set(128, tile8);
        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile9 = new Tile('C', 4);
        Tile tile10 = new Tile('A', 3);
        Tile tile11 = new Tile('T', 5);
        Tile tile12 = new Tile('E', 2);
        Tile tile13 = new Tile('A', 3);

        expectedPlayfield.set(112, tile9);
        expectedPlayfield.set(127, tile10);
        expectedPlayfield.set(142, tile11);
        expectedPlayfield.set(126, tile12);
        expectedPlayfield.set(128, tile13);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void wordIsPlacedHorizontallyAndParallelToExistingWord_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(113, tile2);
        generatedPlayfield.set(114, tile3);

        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('D', 2);
        Tile tile8 = new Tile('O', 6);
        Tile tile9 = new Tile('G', 4);

        userPlayfield.set(112, tile4);
        userPlayfield.set(113, tile5);
        userPlayfield.set(114, tile6);
        userPlayfield.set(128, tile7);
        userPlayfield.set(129, tile8);
        userPlayfield.set(130, tile9);

        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile10 = new Tile('C', 4);
        Tile tile11 = new Tile('A', 3);
        Tile tile12 = new Tile('T', 5);
        Tile tile13 = new Tile('D', 2);
        Tile tile14 = new Tile('O', 6);
        Tile tile15 = new Tile('G', 4);

        expectedPlayfield.set(112, tile10);
        expectedPlayfield.set(113, tile11);
        expectedPlayfield.set(114, tile12);
        expectedPlayfield.set(128, tile13);
        expectedPlayfield.set(129, tile14);
        expectedPlayfield.set(130, tile15);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void wordIsPlacedVerticallyOnPlayfield_validMove_saveUpdatedPlayfield() {
        /* In this test, a new word is placed vertically where the last tile is
        connected to an existing word*/

        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(113, tile2);
        generatedPlayfield.set(114, tile3);

        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('C', 4);
        Tile tile8 = new Tile('A', 3);


        userPlayfield.set(112, tile4);
        userPlayfield.set(113, tile5);
        userPlayfield.set(114, tile6);
        userPlayfield.set(82, tile7);
        userPlayfield.set(97, tile8);

        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile10 = new Tile('C', 4);
        Tile tile11 = new Tile('A', 3);
        Tile tile12 = new Tile('T', 5);
        Tile tile13 = new Tile('C', 4);
        Tile tile14 = new Tile('A', 3);

        expectedPlayfield.set(112, tile10);
        expectedPlayfield.set(113, tile11);
        expectedPlayfield.set(114, tile12);
        expectedPlayfield.set(82, tile13);
        expectedPlayfield.set(97, tile14);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void newWordIsPlacedVerticallyAcrossExistingWord_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(111, tile1);
        generatedPlayfield.set(112, tile2);
        generatedPlayfield.set(113, tile3);

        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('C', 4);
        Tile tile8 = new Tile('R', 7);


        userPlayfield.set(111, tile4);
        userPlayfield.set(112, tile5);
        userPlayfield.set(113, tile6);
        userPlayfield.set(97, tile7);
        userPlayfield.set(127, tile8);

        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile10 = new Tile('C', 4);
        Tile tile11 = new Tile('A', 3);
        Tile tile12 = new Tile('T', 5);
        Tile tile13 = new Tile('C', 4);
        Tile tile14 = new Tile('R', 7);

        expectedPlayfield.set(111, tile10);
        expectedPlayfield.set(112, tile11);
        expectedPlayfield.set(113, tile12);
        expectedPlayfield.set(97, tile13);
        expectedPlayfield.set(127, tile14);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void newWordIsPlacedVerticallyAndParallelToExistingWord_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(97, tile1);
        generatedPlayfield.set(112, tile2);
        generatedPlayfield.set(127, tile3);

        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('C', 4);
        Tile tile8 = new Tile('A', 3);
        Tile tile9 = new Tile('R', 7);

        userPlayfield.set(97, tile4);
        userPlayfield.set(112, tile5);
        userPlayfield.set(127, tile6);
        userPlayfield.set(113, tile7);
        userPlayfield.set(128, tile8);
        userPlayfield.set(143, tile9);

        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile10 = new Tile('C', 4);
        Tile tile11 = new Tile('A', 3);
        Tile tile12 = new Tile('T', 5);
        Tile tile13 = new Tile('C', 4);
        Tile tile14 = new Tile('A', 3);
        Tile tile15 = new Tile('R', 7);

        expectedPlayfield.set(97, tile10);
        expectedPlayfield.set(112, tile11);
        expectedPlayfield.set(127, tile12);
        expectedPlayfield.set(113, tile13);
        expectedPlayfield.set(128, tile14);
        expectedPlayfield.set(143, tile15);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());
    }

    @Test
    public void singleLetterIsPlacedOnPlayfield_validMove_saveUpdatedPlayfield() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);
        Tile tile4 = new Tile('T', 5);
        Tile tile5 = new Tile('T', 5);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(113, tile2);
        generatedPlayfield.set(114, tile3);
        generatedPlayfield.set(115, tile4);
        generatedPlayfield.set(116, tile5);
        generatedPlayfield.set(117, tile6);
        generatedPlayfield.set(118, tile7);

        testGame.setPlayfield(generatedPlayfield);

        // sent Playfield by user
        Game userGame = new Game();
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }

        Tile tile8 = new Tile('C', 4);
        Tile tile9 = new Tile('A', 3);
        Tile tile10 = new Tile('T', 5);
        Tile tile11 = new Tile('T', 5);
        Tile tile12 = new Tile('T', 5);
        Tile tile13 = new Tile('T', 5);
        Tile tile14 = new Tile('T', 5);
        Tile tile15 = new Tile('T', 5);

        userPlayfield.set(112, tile8);
        userPlayfield.set(113, tile9);
        userPlayfield.set(114, tile10);
        userPlayfield.set(115, tile11);
        userPlayfield.set(116, tile12);
        userPlayfield.set(117, tile13);
        userPlayfield.set(118, tile14);
        userPlayfield.set(119, tile15);

        userGame.setPlayfield(userPlayfield);

        // when
        Mockito.when(gameRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testGame));
        gameService.placeTilesOnBoard(userGame);

        // then
        List<Tile> expectedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            expectedPlayfield.add(i, null);
        }
        Tile tile16 = new Tile('C', 4);
        Tile tile17 = new Tile('A', 3);
        Tile tile18 = new Tile('T', 5);
        Tile tile19 = new Tile('T', 5);
        Tile tile20 = new Tile('T', 5);
        Tile tile21 = new Tile('T', 5);
        Tile tile22 = new Tile('T', 5);
        Tile tile23 = new Tile('T', 5);

        expectedPlayfield.set(112, tile16);
        expectedPlayfield.set(113, tile17);
        expectedPlayfield.set(114, tile18);
        expectedPlayfield.set(115, tile19);
        expectedPlayfield.set(116, tile20);
        expectedPlayfield.set(117, tile21);
        expectedPlayfield.set(118, tile22);
        expectedPlayfield.set(119, tile23);

        assertArrayEquals(expectedPlayfield.toArray(), testGame.getPlayfield().toArray());

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
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        userPlayfield.set(113, tile1);
        userPlayfield.set(128, tile2);
        userPlayfield.set(143, tile3);

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
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        userPlayfield.set(112, tile1);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedHorizontally_notConnectedToExistingTile_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(127, tile2);
        generatedPlayfield.set(142, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('E', 2);
        Tile tile8 = new Tile('A', 3);

        userPlayfield.set(112, tile4);
        userPlayfield.set(127, tile5);
        userPlayfield.set(142, tile6);
        userPlayfield.set(144, tile7);
        userPlayfield.set(145, tile8);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedHorizontally_newTilesNotConnected_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(127, tile2);
        generatedPlayfield.set(142, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('E', 2);
        Tile tile8 = new Tile('A', 3);

        userPlayfield.set(112, tile4);
        userPlayfield.set(127, tile5);
        userPlayfield.set(142, tile6);
        userPlayfield.set(141, tile7);
        userPlayfield.set(144, tile8);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedVertically_notConnectedToExistingWord_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(127, tile2);
        generatedPlayfield.set(142, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('E', 2);
        Tile tile8 = new Tile('A', 3);

        userPlayfield.set(112, tile4);
        userPlayfield.set(127, tile5);
        userPlayfield.set(142, tile6);
        userPlayfield.set(144, tile7);
        userPlayfield.set(159, tile8);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordPlacedVertically_newTilesNotConnected_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(127, tile2);
        generatedPlayfield.set(142, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('E', 2);
        Tile tile8 = new Tile('A', 3);

        userPlayfield.set(112, tile4);
        userPlayfield.set(127, tile5);
        userPlayfield.set(142, tile6);
        userPlayfield.set(143, tile7);
        userPlayfield.set(173, tile8);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordIsPlaced_NotHorizontallyNorVertically_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(127, tile2);
        generatedPlayfield.set(142, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('C', 4);
        Tile tile8 = new Tile('A', 3);
        Tile tile9 = new Tile('T', 5);

        userPlayfield.set(112, tile4);
        userPlayfield.set(127, tile5);
        userPlayfield.set(142, tile6);
        userPlayfield.set(143, tile7);
        userPlayfield.set(159, tile8);
        userPlayfield.set(173, tile9);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordIsPlaced_NotHorizontallyNorVertically2_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(113, tile2);
        generatedPlayfield.set(114, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('C', 4);
        Tile tile8 = new Tile('A', 3);
        Tile tile9 = new Tile('T', 5);

        userPlayfield.set(112, tile4);
        userPlayfield.set(113, tile5);
        userPlayfield.set(114, tile6);
        userPlayfield.set(129, tile7);
        userPlayfield.set(145, tile8);
        userPlayfield.set(161, tile9);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void newWordIsPlaced_existingTileIsOverwritten_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(113, tile2);
        generatedPlayfield.set(114, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('C', 4);
        Tile tile8 = new Tile('A', 3);
        Tile tile9 = new Tile('T', 5);

        userPlayfield.set(112, tile4);
        userPlayfield.set(113, tile5);
        userPlayfield.set(114, tile6);
        userPlayfield.set(113, tile7);
        userPlayfield.set(114, tile8);
        userPlayfield.set(115, tile9);

        // when/then
        assertThrows(ResponseStatusException.class, () -> gameService.validMove(userPlayfield, generatedPlayfield));
    }

    @Test
    public void singleTileIsPlaced_NotConnectedToExistingTile_throwError() {
        // given
        // saved Playfield in database
        List<Tile> generatedPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            generatedPlayfield.add(i, null);
        }
        Tile tile1 = new Tile('C', 4);
        Tile tile2 = new Tile('A', 3);
        Tile tile3 = new Tile('T', 5);

        generatedPlayfield.set(112, tile1);
        generatedPlayfield.set(113, tile2);
        generatedPlayfield.set(114, tile3);

        // sent Playfield by user
        List<Tile> userPlayfield = new ArrayList<>();
        for (int i = 0; i < 225; i++){
            userPlayfield.add(i, null);
        }
        Tile tile4 = new Tile('C', 4);
        Tile tile5 = new Tile('A', 3);
        Tile tile6 = new Tile('T', 5);
        Tile tile7 = new Tile('C', 4);

        userPlayfield.set(112, tile4);
        userPlayfield.set(113, tile5);
        userPlayfield.set(114, tile6);
        userPlayfield.set(224, tile7);

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

        List<Tile> handTiles = new ArrayList<>();
        char[] letters = {'A', 'C', 'J', 'K', 'Q', 'A', 'E', 'E'};
        int[] values = {3, 4, 6, 6, 7, 3, 2, 2};

        for (int i = 0; i < letters.length; i++){
            Tile tile = new Tile(letters[i], values[i]);
            handTiles.add(tile);
        }

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
        List<Tile> handTiles = new ArrayList<>();
        char[] letters = {'A', 'C', 'J', 'K', 'Q', 'A', 'E'};
        int[] values = {3, 4, 6, 6, 7, 3, 2};

        for (int i = 0; i < letters.length; i++){
            Tile tile = new Tile(letters[i], values[i]);
            handTiles.add(tile);
        }
        returnedHand.setHandtiles(handTiles);

        // tiles to be swapped
        List<Tile> tilesToBeSwapped = new ArrayList<>();
        char[] letters_tilesToBeSwapped = {'A', 'C', 'Z'};
        int[] values_tilesToBeSwapped = {3, 4, 10};

        for (int j = 0; j < letters_tilesToBeSwapped.length; j++){
            Tile tileToBeSwapped = new Tile(letters_tilesToBeSwapped[j], values_tilesToBeSwapped[j]);
            tilesToBeSwapped.add(tileToBeSwapped);
        }

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
        List<Tile> handTiles = new ArrayList<>();
        char[] letters = {'A', 'C', 'J', 'K', 'Q', 'A', 'E'};
        int[] values = {3, 4, 6, 6, 7, 3, 2};

        for (int i = 0; i < letters.length; i++){
            Tile tile = new Tile(letters[i], values[i]);
            handTiles.add(tile);
        }
        returnedHand.setHandtiles(handTiles);

        // tiles to be swapped
        List<Tile> tilesToBeSwapped = new ArrayList<>();
        char[] letters_tilesToBeSwapped = {'A', 'C', 'J'};
        int[] values_tilesToBeSwapped = {3, 4, 6};

        for (int j = 0; j < letters_tilesToBeSwapped.length; j++){
            Tile tileToBeSwapped = new Tile(letters_tilesToBeSwapped[j], values_tilesToBeSwapped[j]);
            tilesToBeSwapped.add(tileToBeSwapped);
        }

        Game returnedGame = new Game();
        Bag returnedBag = new Bag();
        returnedBag.setId(1L);

        // tiles in Bag
        List<Tile> tilesInBag = new ArrayList<>();
        char[] lettersInBag = {'Q', 'T'};
        int[] valuesInBag = {7, 6};

        for (int j = 0; j < lettersInBag.length; j++){
            Tile tileInBag = new Tile(lettersInBag[j], valuesInBag[j]);
            tilesInBag.add(tileInBag);
        }
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
        List<Tile> handTiles = new ArrayList<>();
        char[] letters = {'A', 'C', 'J', 'K', 'Q', 'A', 'E'};
        int[] values = {3, 4, 6, 6, 7, 3, 2};

        for (int i = 0; i < letters.length; i++){
            Tile tile = new Tile(letters[i], values[i]);
            handTiles.add(tile);
        }
        returnedHand.setHandtiles(handTiles);

        // tiles to be swapped
        List<Tile> tilesToBeSwapped = new ArrayList<>();
        char[] letters_tilesToBeSwapped = {'A', 'C', 'Q'};
        int[] values_tilesToBeSwapped = {3, 4, 7};

        for (int j = 0; j < letters_tilesToBeSwapped.length; j++){
            Tile tileToBeSwapped = new Tile(letters_tilesToBeSwapped[j], values_tilesToBeSwapped[j]);
            tilesToBeSwapped.add(tileToBeSwapped);
        }

        // tiles in Bag
        Game returnedGame = new Game();
        Bag returnedBag = new Bag();
        returnedBag.setId(1L);

        List<Tile> tilesInBag = new ArrayList<>();
        char[] lettersInBag = {'Q', 'T', 'M'};
        int[] valuesInBag = {7, 6, 4};

        for (int j = 0; j < lettersInBag.length; j++){
            Tile tileInBag = new Tile(lettersInBag[j], valuesInBag[j]);
            tilesInBag.add(tileInBag);
        }
        returnedBag.setTiles(tilesInBag);
        returnedGame.setBag(returnedBag);

        given(handRepository.findById(Mockito.any())).willReturn(Optional.of(returnedHand));
        given(gameRepository.findById(Mockito.any())).willReturn(Optional.of(returnedGame));

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
}
