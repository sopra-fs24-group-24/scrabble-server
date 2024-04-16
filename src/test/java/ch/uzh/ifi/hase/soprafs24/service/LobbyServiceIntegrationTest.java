package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
public class LobbyServiceIntegrationTest {

    @Qualifier("lobbyRepository")
    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private LobbyService lobbyService;

    @BeforeEach
    public void setup() { lobbyRepository.deleteAll(); }

    @Test
    public void createLobby_validInputs_success() {
        // given
        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);

        // when
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // then
        assertEquals(testLobby.getId(), createdLobby.getId());
        assertEquals(testLobby.getUsersInLobby(), createdLobby.getUsersInLobby());
        assertEquals(testLobby.getLobbySize(), createdLobby.getLobbySize());
        assertEquals(1, createdLobby.getNumberOfPlayers());
        assertFalse(createdLobby.getGameStarted());
    }

    @Test
    public void createLobby_UserAlreadyInLobby_throwsException() {
        // given
        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // attempt to create another Lobby with the same user
        Lobby testLobby2 = new Lobby();
        List<Long> players2 = new ArrayList<Long>();
        players2.add(1L);
        testLobby2.setUsersInLobby(players2);
        testLobby2.setLobbySize(2);

        // check that an error is thrown
        assertThrows(ResponseStatusException.class, () -> lobbyService.createLobby(testLobby2));
    }

    @Test
    public void addPlayerToLobby_validInputs_thenPlayerAddedToLobby() {
        // given
        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), 2L);

        // then
        assertEquals(3, updatedLobby.getLobbySize());
        assertEquals(2, updatedLobby.getNumberOfPlayers());
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);
        assertArrayEquals(expectedPlayers.toArray(), updatedLobby.getUsersInLobby().toArray());
        assertFalse(updatedLobby.getGameStarted());
    }

    @Test
    public void addPlayerToLobby_validInputs_lobbyFull_thenGameIsStarted() {
        // given
        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);

        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), 2L);

        // then
        assertEquals(2, updatedLobby.getLobbySize());
        assertEquals(2, updatedLobby.getNumberOfPlayers());
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);
        assertArrayEquals(expectedPlayers.toArray(), updatedLobby.getUsersInLobby().toArray());
        assertTrue(updatedLobby.getGameStarted());
    }


    @Test
    public void addPlayerToLobby_NonExistentLobby_throwsException() {
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(20L, 1L));
    }

    @Test
    public void addPlayerToLobby_UserAlreadyInLobby_throwsException() {
        // given
        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        Lobby testLobby2 = new Lobby();
        List<Long> players2 = new ArrayList<Long>();
        players2.add(3L);
        testLobby2.setUsersInLobby(players2);
        testLobby2.setLobbySize(4);

        Lobby createdLobby = lobbyService.createLobby(testLobby);
        Lobby createdLobby2 = lobbyService.createLobby(testLobby2);

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(createdLobby2.getId(), 1L));

    }

    @Test
    public void addPlayerToLobby_LobbyAlreadyFull_throwsException() {
        // given
        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);

        Lobby createdLobby = lobbyService.createLobby(testLobby);
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), 2L);

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(updatedLobby.getId(), 3L));


    }
}
