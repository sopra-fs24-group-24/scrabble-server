package ch.uzh.ifi.hase.soprafs24.repository;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DataJpaTest
public class LobbyRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LobbyRepository lobbyRepository;

    @Test
    public void findLobbyByUserId_success() {
        // given
        Lobby lobby = new Lobby();
        lobby.setLobbySize(4);
        lobby.setNumberOfPlayers(2);
        lobby.setGameStarted(false);
        List<Long> players = new ArrayList<Long>();
        players.add(1L);
        players.add(2L);
        lobby.setUsersInLobby(players);

        entityManager.persist(lobby);
        entityManager.flush();

        // when
        Lobby found = lobbyRepository.findLobbyByUserId(1L).orElseThrow(() -> new RuntimeException("Lobby not found"));

        // then
        assertEquals(found.getId(), lobby.getId());
        assertEquals(found.getUsersInLobby(), lobby.getUsersInLobby());
        assertEquals(found.getLobbySize(), lobby.getLobbySize());
        assertEquals(found.getNumberOfPlayers(), lobby.getNumberOfPlayers());
        assertEquals(found.getGameStarted(), lobby.getGameStarted());
    }
}
