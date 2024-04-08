package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
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

public class LobbyServiceTest {

    @Mock
    private LobbyRepository lobbyRepository;

    @InjectMocks
    private LobbyService lobbyService;

    private Lobby testLobby;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        testLobby = new Lobby();
        testLobby.setId(1L);
        testLobby.setLobbySize(4);
        testLobby.setNumberOfPlayers(1);
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setGameStarted(false);

        // when
        Mockito.when(lobbyRepository.save(Mockito.any())).thenReturn(testLobby);
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
        assertEquals(testLobby.getGameStarted(), createdLobby.getGameStarted());
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

}
