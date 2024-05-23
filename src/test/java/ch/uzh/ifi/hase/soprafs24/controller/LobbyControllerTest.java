package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    @Mock
    private UserRepository userRepository;

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
                .contentType(MediaType.APPLICATION_JSON).header("token", "4242");

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

        User user = new User();
        user.setId(1L);
        user.setToken("1");

        User user2 = new User();
        user2.setId(3L);
        user2.setToken("3");

        given(lobbyService.getLobby(2L)).willReturn(lobby);
        given(userService.isTokenValid("4242")).willReturn(user);
   

        // when
        MockHttpServletRequestBuilder getRequest = get("/lobbies/{lobbyId}", 2L)
                .contentType(MediaType.APPLICATION_JSON).header("token", "4242");

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
                .contentType(MediaType.APPLICATION_JSON).header("token", "4242");

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
                .content(asJsonString(lobbyPostDTO)).header("token", "4242");

        // then
        mockMvc.perform(postRequest).andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.numberOfPlayers", is(lobby.getNumberOfPlayers())))
                .andExpect(jsonPath("$.lobbySize", is(lobby.getLobbySize())))
                .andExpect(jsonPath("$.usersInLobby", contains(lobby.getUsersInLobby().get(0).intValue())))
                .andExpect(jsonPath("$.usersInLobby").value(hasSize(1)))
                .andExpect(jsonPath("$.gameOfLobby").isEmpty())
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
                .content(asJsonString(lobbyPostDTO)).header("token", "4242");

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
                .content(asJsonString(userId)).header("token", "4242");

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(lobby.getId().intValue())))
                .andExpect(jsonPath("$.numberOfPlayers", is(lobby.getNumberOfPlayers())))
                .andExpect(jsonPath("$.lobbySize", is(lobby.getLobbySize())))
                .andExpect(jsonPath("$.usersInLobby", containsInAnyOrder(lobby.getUsersInLobby().get(0).intValue(), lobby.getUsersInLobby().get(1).intValue())))
                .andExpect(jsonPath("$.usersInLobby").value(hasSize(2)))
                .andExpect(jsonPath("$.gameOfLobby").isEmpty())
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
                .content(asJsonString(userId)).header("token", "4242");

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
                .content(asJsonString(userId)).header("token", "4242");

        // then
        mockMvc.perform(putRequest).andExpect(status().isConflict())
                .andExpect(content().string(""));
    }

    @Test
    public void addPlayerToPrivateLobby_validInputs_PlayerAddedToPrivateLobby() throws Exception{
        // given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("fabio");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("luca");
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("martin");
        user3.setToken("3");

        // old Lobby
        Lobby oldLobby = new Lobby();
        oldLobby.setId(6L);
        oldLobby.setLobbySize(4);
        oldLobby.setNumberOfPlayers(2);
        List<Long> playersIdOld = new ArrayList<Long>();
        playersIdOld.add(user1.getId());
        playersIdOld.add(user2.getId());
        oldLobby.setUsersInLobby(playersIdOld);
        List<User> playersOld = new ArrayList<>();
        playersOld.add(user1);
        playersOld.add(user2);
        oldLobby.setPlayers(playersOld);
        oldLobby.setGameStarted(false);
        oldLobby.setIsPrivate(true);
        oldLobby.setPin("100000");

        // new Lobby
        Lobby newLobby = new Lobby();
        newLobby.setId(6L);
        newLobby.setLobbySize(4);
        newLobby.setNumberOfPlayers(3);
        List<Long> playersID = new ArrayList<Long>();
        playersID.add(user1.getId());
        playersID.add(user2.getId());
        playersID.add(user3.getId());
        newLobby.setUsersInLobby(playersID);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);
        newLobby.setPlayers(players);
        newLobby.setGameStarted(false);
        newLobby.setGameOfLobby(null);
        newLobby.setIsPrivate(true);
        newLobby.setPin("100000");

        given(userRepository.findByToken(Mockito.any())).willReturn(user3);
        given(lobbyService.checkIfLobbyExistsByPin(Mockito.any())).willReturn(oldLobby);
        given(lobbyService.addPlayertoLobby(Mockito.any(), Mockito.any())).willReturn(newLobby);

        // add new player to Lobby
        Map<String, Long> userId = new HashMap<>();
        userId.put("userId", user3.getId());

        // when
        MockHttpServletRequestBuilder putRequest = put("/privatelobbies/6")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userId)).header("token", user3.getId());

        // then
        mockMvc.perform(putRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(newLobby.getId().intValue())))
                .andExpect(jsonPath("$.numberOfPlayers", is(newLobby.getNumberOfPlayers())))
                .andExpect(jsonPath("$.lobbySize", is(newLobby.getLobbySize())))
                .andExpect(jsonPath("$.usersInLobby", containsInAnyOrder(newLobby.getUsersInLobby().get(0).intValue(), newLobby.getUsersInLobby().get(1).intValue(), newLobby.getUsersInLobby().get(2).intValue())))
                .andExpect(jsonPath("$.usersInLobby").value(hasSize(3)))
                .andExpect(jsonPath("$.players").value(hasSize(3)))
                .andExpect(jsonPath("$.gameOfLobby").isEmpty())
                .andExpect(jsonPath("$.gameStarted", is(newLobby.getGameStarted())));
    }

    @Test
    public void addPlayerToPrivateLobby_invalidInputs_PlayerAddedToPrivateLobby() throws Exception{
        // given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("fabio");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("luca");
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("martin");
        user3.setToken("3");

        given(userService.isTokenValid(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        // add new player to Lobby
        Map<String, Long> userId = new HashMap<>();
        userId.put("userId", user3.getId());

        // when
        MockHttpServletRequestBuilder putRequest = put("/privatelobbies/6")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userId)).header("token", user3.getId());

        // then
        mockMvc.perform(putRequest).andExpect(status().isForbidden())
                .andExpect(content().string(""));
    }

    @Test
    public void removePlayerFromLobby_validInputs_playerRemovedFromLobby() throws Exception {
        // given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("fabio");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("luca");
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("martin");
        user3.setToken("3");

        Lobby newLobby = new Lobby();
        newLobby.setId(6L);
        newLobby.setLobbySize(4);
        newLobby.setNumberOfPlayers(3);
        List<Long> playersID = new ArrayList<Long>();
        playersID.add(user1.getId());
        playersID.add(user2.getId());
        playersID.add(user3.getId());
        newLobby.setUsersInLobby(playersID);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);
        newLobby.setPlayers(players);
        newLobby.setGameStarted(false);
        newLobby.setGameOfLobby(null);

        given(userRepository.findByToken(Mockito.any())).willReturn(user3);

        // remove player from Lobby
        Map<String, Long> userId = new HashMap<>();
        userId.put("userId", user3.getId());

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/withdrawal/6")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userId)).header("token", user3.getId());

        // then
        mockMvc.perform(putRequest).andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    public void removePlayerFromLobby_invalidInputs_playerRemovedFromLobby() throws Exception {
        // given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("fabio");
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("luca");
        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("martin");
        user3.setToken("3");

        Lobby newLobby = new Lobby();
        newLobby.setId(6L);
        newLobby.setLobbySize(4);
        newLobby.setNumberOfPlayers(3);
        List<Long> playersID = new ArrayList<Long>();
        playersID.add(user1.getId());
        playersID.add(user2.getId());
        playersID.add(user3.getId());
        newLobby.setUsersInLobby(playersID);
        List<User> players = new ArrayList<>();
        players.add(user1);
        players.add(user2);
        players.add(user3);
        newLobby.setPlayers(players);
        newLobby.setGameStarted(false);
        newLobby.setGameOfLobby(null);

        given(userService.isTokenValid(Mockito.any())).willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        // remove player from Lobby
        Map<String, Long> userId = new HashMap<>();
        userId.put("userId", user3.getId());

        // when
        MockHttpServletRequestBuilder putRequest = put("/lobbies/withdrawal/6")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userId)).header("token", user3.getId());

        // then
        mockMvc.perform(putRequest).andExpect(status().isForbidden())
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
/*
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
                .content(asJsonString(lobby)).header("token", "4242");

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void deleteNonExistentLobby() throws Exception
    {
        Lobby lobby=new Lobby();
        lobby.setId(1L);
        lobby.setNumberOfPlayers(1);
        lobby.setLobbySize(1);

        User user=new User();
        user.setId(42L);

        List<Long> users=new ArrayList<Long>();
        users.add(user.getId());

        lobby.setUsersInLobby(users);

        ResponseStatusException e=new ResponseStatusException(HttpStatus.NOT_FOUND);

        given(lobbyService.checkIfLobbyExistsById(1L)).willThrow(e);
        given(userService.isTokenValid(Mockito.any())).willReturn(user);

        // when
        MockHttpServletRequestBuilder deleteRequest = delete("/lobbies/{lobbyId}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(lobby)).header("token", "4242");

        // then
        mockMvc.perform(deleteRequest).andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }*/
}
