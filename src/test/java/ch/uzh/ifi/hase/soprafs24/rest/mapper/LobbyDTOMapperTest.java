package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LobbyDTOMapperTest {
    @Test
    public void testCreateLobby_fromLobbyPostDTO_toLobby_success() {
        // create LobbyPostDTO
        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbySize(4);
        List<Long> players = new ArrayList<>();
        players.add(1L);
        lobbyPostDTO.setUsersInLobby(players);

        // MAP -> Create Lobby
        Lobby lobby = LobbyDTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

        // check content
        assertEquals(lobbyPostDTO.getLobbySize(), lobby.getLobbySize());
        assertEquals(lobbyPostDTO.getUsersInLobby(), lobby.getUsersInLobby());
    }

    @Test
    public void testGetLobby_fromLobby_toLobbyGetDTO_success() {
        // create Lobby
        Lobby lobby = new Lobby();
        lobby.setLobbySize(3);
        lobby.setNumberOfPlayers(1);
        lobby.setGameStarted(false);
        List<Long> players = new ArrayList<>();
        players.add(1L);
        lobby.setUsersInLobby(players);

        // MAP -> Create LobbyGetDTO
        LobbyGetDTO lobbyGetDTO = LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);

        // check content
        assertEquals(lobby.getId(), lobbyGetDTO.getId());
        assertEquals(lobby.getNumberOfPlayers(), lobbyGetDTO.getNumberOfPlayers());
        assertEquals(lobby.getLobbySize(), lobbyGetDTO.getLobbySize());
        assertEquals(lobby.getUsersInLobby(), lobbyGetDTO.getUsersInLobby());
        assertEquals(lobby.getGameStarted(), lobbyGetDTO.getGameStarted());
    }
}
