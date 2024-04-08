package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LobbyController.class)
public class LobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LobbyService lobbyService;

    @Test
    public void givenLobbies_whenGetLobbies_thenReturnJsonArray() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(1L);
        lobby.setLobbySize(4);
        lobby.setNumberOfPlayers(1);
        lobby.setGameStarted(false);
        List<Long> players = new ArrayList<Long>();
        players.add((long) 0);
        lobby.setUsersInLobby(players);

        List<Lobby> allLobbies = Collections.singletonList(lobby);

        given(lobbyService.getLobbies()).willReturn(allLobbies);

        // when
        MockHttpServletRequestBuilder getRequest = get("/lobbies")
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$[0].numberOfPlayers", is(lobby.getNumberOfPlayers())))
                .andExpect(jsonPath("$[0].lobbySize", is(lobby.getLobbySize())))
                .andExpect(jsonPath("$[0].usersInLobby", contains(lobby.getUsersInLobby().get(0).intValue())))
                .andExpect(jsonPath("$[0].usersInLobby").value(hasSize(1)))
                .andExpect(jsonPath("$[0].gameStarted", is(lobby.getGameStarted())));
    }

    @Test
    public void getLobby_validInput() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(2L);
        lobby.setLobbySize(4);
        lobby.setNumberOfPlayers(2);
        lobby.setGameStarted(false);
        List<Long> players = new ArrayList<Long>();
        players.add((long) 1);
        players.add((long) 3);
        lobby.setUsersInLobby(players);

        given(lobbyService.getLobby(2L)).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder getRequest = get("/lobbies/{lobbyId}", 2L)
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.numberOfPlayers", is(lobby.getNumberOfPlayers())))
                .andExpect(jsonPath("$.lobbySize", is(lobby.getLobbySize())))
                .andExpect(jsonPath("$.usersInLobby", containsInAnyOrder(lobby.getUsersInLobby().get(1).intValue(), lobby.getUsersInLobby().get(0).intValue())))
                .andExpect(jsonPath("$.usersInLobby").value(hasSize(2)))
                .andExpect(jsonPath("$.gameStarted", is(lobby.getGameStarted())));
    }
}
