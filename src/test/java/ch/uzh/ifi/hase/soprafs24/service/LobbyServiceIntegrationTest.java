package ch.uzh.ifi.hase.soprafs24.service;


import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebAppConfiguration
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LobbyServiceIntegrationTest {

    @Qualifier("lobbyRepository")
    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private LobbyService lobbyService;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        lobbyRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void createLobby_validInputs_success() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        Lobby testLobby = new Lobby();
        List<Long> playersId = new ArrayList<Long>();
        playersId.add(1L);
        testLobby.setUsersInLobby(playersId);
        testLobby.setLobbySize(2);

        // when
        User createdUser = userService.createUser(testUser);
        System.out.println(createdUser.getId());
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // then
        //assertEquals(testLobby.getId(), createdLobby.getId());
        assertEquals(testLobby.getUsersInLobby(), createdLobby.getUsersInLobby());
        assertEquals(createdUser.getId(), createdLobby.getPlayers().get(0).getId());
        assertEquals(createdUser.getUsername(), createdLobby.getPlayers().get(0).getUsername());
        assertEquals(createdUser.getToken(), createdLobby.getPlayers().get(0).getToken());
        assertEquals(createdUser.getPassword(), createdLobby.getPlayers().get(0).getPassword());
        assertEquals(testLobby.getLobbySize(), createdLobby.getLobbySize());
        assertEquals(1, createdLobby.getNumberOfPlayers());
        assertFalse(createdLobby.getGameStarted());
        assertNull(createdLobby.getGameOfLobby());
    }

    @Test
    public void createLobby_UserAlreadyInLobby_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        userService.createUser(testUser);
        lobbyService.createLobby(testLobby);

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
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        User createdUser1 = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser2);
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), 2L);

        // then
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);

        assertEquals(3, updatedLobby.getLobbySize());
        assertEquals(2, updatedLobby.getNumberOfPlayers());
        assertArrayEquals(expectedPlayers.toArray(), updatedLobby.getUsersInLobby().toArray());
        assertFalse(updatedLobby.getGameStarted());
        assertNull(updatedLobby.getGameOfLobby());

        assertEquals(createdUser1.getId(), updatedLobby.getPlayers().get(0).getId());
        assertEquals(createdUser1.getUsername(), updatedLobby.getPlayers().get(0).getUsername());
        assertEquals(createdUser1.getToken(), updatedLobby.getPlayers().get(0).getToken());
        assertEquals(createdUser1.getPassword(), updatedLobby.getPlayers().get(0).getPassword());

        assertEquals(createdUser2.getId(), updatedLobby.getPlayers().get(1).getId());
        assertEquals(createdUser2.getUsername(), updatedLobby.getPlayers().get(1).getUsername());
        assertEquals(createdUser2.getToken(), updatedLobby.getPlayers().get(1).getToken());
        assertEquals(createdUser2.getPassword(), updatedLobby.getPlayers().get(1).getPassword());

    }

    @Test
    public void addPlayerToLobby_validInputs_lobbyFull_thenGameIsStarted() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);

        User createdUser1 = userService.createUser(testUser);
        User createdUser2 = userService.createUser(testUser2);
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), 2L);

        // then
        List<Long> expectedPlayers = new ArrayList<>();
        expectedPlayers.add(1L);
        expectedPlayers.add(2L);

        assertEquals(2, updatedLobby.getLobbySize());
        assertEquals(2, updatedLobby.getNumberOfPlayers());
        assertArrayEquals(expectedPlayers.toArray(), updatedLobby.getUsersInLobby().toArray());
        assertTrue(updatedLobby.getGameStarted());
        assertNotNull(updatedLobby.getGameOfLobby());

        assertEquals(createdUser1.getId(), updatedLobby.getPlayers().get(0).getId());
        assertEquals(createdUser1.getUsername(), updatedLobby.getPlayers().get(0).getUsername());
        assertEquals(createdUser1.getToken(), updatedLobby.getPlayers().get(0).getToken());
        assertEquals(createdUser1.getPassword(), updatedLobby.getPlayers().get(0).getPassword());

        assertEquals(createdUser2.getId(), updatedLobby.getPlayers().get(1).getId());
        assertEquals(createdUser2.getUsername(), updatedLobby.getPlayers().get(1).getUsername());
        assertEquals(createdUser2.getToken(), updatedLobby.getPlayers().get(1).getToken());
        assertEquals(createdUser2.getPassword(), updatedLobby.getPlayers().get(1).getPassword());
    }


    @Test
    public void addPlayerToLobby_NonExistentLobby_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        userService.createUser(testUser);

        // when/then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(20L, 1L));
    }

    @Test
    public void addPlayerToLobby_UserAlreadyInLobby_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(3);

        Lobby testLobby2 = new Lobby();
        List<Long> players2 = new ArrayList<Long>();
        players2.add(2L);
        testLobby2.setUsersInLobby(players2);
        testLobby2.setLobbySize(4);

        User createdUser = userService.createUser(testUser);
        userService.createUser(testUser2);
        lobbyService.createLobby(testLobby);
        Lobby createdLobby2 = lobbyService.createLobby(testLobby2);

        // when/then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(createdLobby2.getId(), createdUser.getId()));

    }

    @Test
    public void addPlayerToLobby_LobbyAlreadyFull_throwsException() {
        // given
        User testUser = new User();
        testUser.setUsername("fabio");
        testUser.setPassword("1");

        User testUser2 = new User();
        testUser2.setUsername("manuel");
        testUser2.setPassword("2");

        User testUser3 = new User();
        testUser3.setUsername("joel");
        testUser3.setPassword("3");

        Lobby testLobby = new Lobby();
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        testLobby.setUsersInLobby(players);
        testLobby.setLobbySize(2);

        userService.createUser(testUser);
        User createdUser = userService.createUser(testUser2);
        User createdUser2 = userService.createUser(testUser3);
        Lobby createdLobby = lobbyService.createLobby(testLobby);

        // when
        Lobby updatedLobby = lobbyService.addPlayertoLobby(createdLobby.getId(), createdUser.getId());

        // then
        assertThrows(ResponseStatusException.class, () -> lobbyService.addPlayertoLobby(updatedLobby.getId(), createdUser2.getId()));

    }
}
