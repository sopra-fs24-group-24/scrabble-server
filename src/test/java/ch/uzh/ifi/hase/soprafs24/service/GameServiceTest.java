package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Game;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

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
}
