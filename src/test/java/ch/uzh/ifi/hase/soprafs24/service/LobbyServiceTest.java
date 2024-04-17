package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
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

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LobbyService lobbyService;

    @InjectMocks
    private UserService userService;

    private Lobby testLobby;
    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

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
        User createdUser = userService.createUser(testUser);
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

    @Test
    public void addPlayerToLobby_validInputs_thenPlayerAddedToLobby() {
        // given
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(null));
        Mockito.when(lobbyRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(testLobby));
        lobbyService.addPlayertoLobby(1L, 2L);

        // then
        assertEquals(1L, createdLobby.getId());
        assertEquals(4, createdLobby.getLobbySize());
        assertEquals(2, createdLobby.getNumberOfPlayers());
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);
        assertArrayEquals(expectedPlayers.toArray(), createdLobby.getUsersInLobby().toArray());
        assertFalse(createdLobby.getGameStarted());
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
        assertEquals(1L, createdLobby.getId());
        assertEquals(4, createdLobby.getLobbySize());
        assertEquals(4, createdLobby.getNumberOfPlayers());
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);
        expectedPlayers.add(3L);
        expectedPlayers.add(4L);
        assertArrayEquals(expectedPlayers.toArray(), createdLobby.getUsersInLobby().toArray());
        assertTrue(createdLobby.getGameStarted());
    }


    @Test
    public void addPlayerToLobby_NonExistentLobby_throwsException() {
        // given
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Mockito.when(lobbyRepository.findLobbyByUserId(Mockito.any())).thenReturn(Optional.ofNullable(null));
        Mockito.when(lobbyRepository.findById(Mockito.any())).thenReturn(Optional.ofNullable(null));

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(3L, 2L));
    }

    @Test
    public void addPlayerToLobby_UserAlreadyInLobby_throwsException() {
        // given
        Lobby createdLobby = lobbyService.createLobby(testLobby);

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
}
