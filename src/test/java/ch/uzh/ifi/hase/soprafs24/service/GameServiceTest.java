package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.dictionary.Dictionary;
import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private Dictionary dictionary;

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
}
