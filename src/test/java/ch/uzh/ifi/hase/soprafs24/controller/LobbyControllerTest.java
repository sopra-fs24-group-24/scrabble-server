package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LobbyController.class)
public class LobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LobbyService lobbyService;

    @MockBean
    private UserService userService;

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
    public void getLobby_whenRequestExistentLobby_thenReturnLobby() throws Exception {
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

    @Test
    public void getLobby_whenRequestNonExistentLobby_thenThrowError() throws Exception {
        // given
        long lobbyId = 5;

        given(lobbyService.getLobby(lobbyId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // when
        MockHttpServletRequestBuilder getRequest = get("/lobbies/{lobbyId}", lobbyId)
                .contentType(MediaType.APPLICATION_JSON);

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void createLobby_whenValidInput_thenLobbyCreated() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(4L);
        lobby.setLobbySize(4);
        lobby.setNumberOfPlayers(1);
        lobby.setGameStarted(false);
        List<Long> players = new ArrayList<Long>();
        players.add((long) 4);
        lobby.setUsersInLobby(players);

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbySize(4);
        lobbyPostDTO.setUsersInLobby(players);

        given(lobbyService.createLobby(Mockito.any())).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));

        // then
        mockMvc.perform(postRequest).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.numberOfPlayers", is(lobby.getNumberOfPlayers())))
                .andExpect(jsonPath("$.lobbySize", is(lobby.getLobbySize())))
                .andExpect(jsonPath("$.usersInLobby", contains(lobby.getUsersInLobby().get(0).intValue())))
                .andExpect(jsonPath("$.usersInLobby").value(hasSize(1)))
                .andExpect(jsonPath("$.gameStarted", is(lobby.getGameStarted())));

    }

    @Test
    public void createLobby_whenUserAlreadyInLobby_thenThrowError() throws Exception {
        // given
        Lobby lobby = new Lobby();
        lobby.setId(6L);
        lobby.setLobbySize(3);
        lobby.setNumberOfPlayers(1);
        lobby.setGameStarted(false);
        List<Long> players = new ArrayList<Long>();
        players.add((long) 6);
        lobby.setUsersInLobby(players);

        LobbyPostDTO lobbyPostDTO = new LobbyPostDTO();
        lobbyPostDTO.setLobbySize(3);
        lobbyPostDTO.setUsersInLobby(players);

        given(lobbyService.createLobby(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        // when
        MockHttpServletRequestBuilder postRequest = post("/lobbies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobbyPostDTO));

        // then
        mockMvc.perform(postRequest).andExpect(status().isConflict())
                .andExpect(content().string(""));
    }

    @Test
    public void addPlayerToLobby_validInputs_thenPlayerAddedToLobby() throws Exception {
        // given
        Lobby lobby = new Lobby();
        Long lobbyId = 6L;
        lobby.setId(lobbyId);
        lobby.setLobbySize(4);
        lobby.setNumberOfPlayers(2);
        List<Long> players = new ArrayList<Long>();
        players.add(7L);
        players.add(10L);
        lobby.setUsersInLobby(players);
        lobby.setGameStarted(false);

        // add new player to Lobby
        Map<String, Long> userId = new HashMap<>();
        userId.put("userId", 10L);

        given(lobbyService.addPlayertoLobby(lobbyId, 10L)).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/{lobbyId}", lobbyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userId));

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.numberOfPlayers", is(lobby.getNumberOfPlayers())))
                .andExpect(jsonPath("$.lobbySize", is(lobby.getLobbySize())))
                .andExpect(jsonPath("$.usersInLobby", containsInAnyOrder(lobby.getUsersInLobby().get(0).intValue(), lobby.getUsersInLobby().get(1).intValue())))
                .andExpect(jsonPath("$.usersInLobby").value(hasSize(2)))
                .andExpect(jsonPath("$.gameStarted", is(lobby.getGameStarted())));


    }

    @Test
    public void addPlayerToLobby_whenNonExistentLobby_thenThrowError() throws Exception {
        // given
        Long lobbyId = 22L;
        Map<String, Long> userId = new HashMap<>();
        userId.put("userId", 10L);


        given(lobbyService.addPlayertoLobby(lobbyId, 10L)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/{lobbyId}", lobbyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userId));

        // then
        mockMvc.perform(putRequest).andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    public void addPlayerToLobby_whenUserAlreadyInLobby_thenThrowError() throws Exception {
        // given
        Long lobbyId = 30L;
        Map<String, Long> userId = new HashMap<>();
        userId.put("userId", 12L);


        given(lobbyService.addPlayertoLobby(lobbyId, 12L)).willThrow(new ResponseStatusException(HttpStatus.CONFLICT));

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/{lobbyId}", lobbyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userId));

        // then
        mockMvc.perform(putRequest).andExpect(status().isConflict())
                .andExpect(content().string(""));
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input
     * can be processed
     *
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }

    @Test
    public void deleteExistingLobby() throws Exception 
    {
        Lobby lobby=new Lobby();
        lobby.setId(1L);
        lobby.setNumberOfPlayers(1);
        lobby.setLobbySize(1);

        given(lobbyService.checkIfLobbyExistsById(1L)).willReturn(lobby);

        // when
        MockHttpServletRequestBuilder deleteRequest = delete("/lobbies/{lobbyId}", lobby.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobby));

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteNonExistantLobby() throws Exception 
    {
        Lobby lobby=new Lobby();
        lobby.setId(1L);
        lobby.setNumberOfPlayers(1);
        lobby.setLobbySize(1);

        ResponseStatusException e=new ResponseStatusException(HttpStatus.NOT_FOUND);

        given(lobbyService.checkIfLobbyExistsById(1L)).willThrow(e);

        // when
        MockHttpServletRequestBuilder deleteRequest = delete("/lobbies/{lobbyId}", lobby.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobby));

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }
}
