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
}
