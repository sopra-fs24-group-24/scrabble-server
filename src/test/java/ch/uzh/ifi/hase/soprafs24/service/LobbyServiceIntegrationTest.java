package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
        assertEquals(createdLobby.getNumberOfPlayers(), 1);
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

}
