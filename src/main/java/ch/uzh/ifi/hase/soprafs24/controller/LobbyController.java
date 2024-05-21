package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.LobbyDTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.LobbyService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class LobbyController {

    private final LobbyService lobbyService;

    @Autowired
    private final UserService userService;

    LobbyController(LobbyService lobbyService, UserService userService) 
    {
        this.lobbyService = lobbyService;
        this.userService = userService;
    }

    @GetMapping("/lobbies")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<LobbyGetDTO> getAllLobbies(@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        List<Lobby> lobbies = lobbyService.getLobbies();
        List<LobbyGetDTO> lobbyGetDTOs = new ArrayList<>();
        for (Lobby lobby : lobbies) {
            LobbyGetDTO lobbyGetDTO = LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
            lobbyService.transformUsersIntoUsersSlim(lobbyGetDTO, lobby);
            lobbyGetDTOs.add(lobbyGetDTO);
        }
        return lobbyGetDTOs;
    }

    @GetMapping("/lobbies/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO getSpecificLobby(@PathVariable Long lobbyId,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        User foundUser = userService.isTokenValid(token);
        Lobby lobby = lobbyService.getLobby(lobbyId);
        LobbyGetDTO lobbyGetDTO = LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby);
        lobbyService.removeHandsFromOtherPlayers(lobbyGetDTO, foundUser.getId());
        lobbyService.transformUsersIntoUsersSlim(lobbyGetDTO, lobby);
        return lobbyGetDTO;
    }

    @PostMapping("/lobbies")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public LobbyGetDTO createLobby(@RequestBody LobbyPostDTO lobbyPostDTO,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        // convert API user to internal representation
        Lobby userInput = LobbyDTOMapper.INSTANCE.convertLobbyPostDTOtoEntity(lobbyPostDTO);

        // create lobby
        Lobby createdLobby = lobbyService.createLobby(userInput);
        // convert internal representation of user back to API
        return LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(createdLobby);
    }

    @PutMapping("/lobbies/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyGetDTO joinLobby(@PathVariable Long lobbyId, @RequestBody Map<String, Long> requestBody,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        Long userId = requestBody.get("userId");
        Lobby joinedLobby = lobbyService.addPlayertoLobby(lobbyId, userId);
        LobbyGetDTO lobbyGetDTO = LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(joinedLobby);
        lobbyService.removeHandsFromOtherPlayers(lobbyGetDTO, userId);
        lobbyService.transformUsersIntoUsersSlim(lobbyGetDTO, joinedLobby);
        return lobbyGetDTO;
    }

    @PutMapping("/privatelobbies/{lobbyPin}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    
    public LobbyGetDTO joinPrivateLobby(@PathVariable String lobbyPin, @RequestBody Map<String, Long> requestBody,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        Long userId = requestBody.get("userId");
        Lobby lobby = lobbyService.checkIfLobbyExistsByPin(lobbyPin);
        Lobby joinedLobby = lobbyService.addPlayertoLobby(lobby.getId(), userId);
        LobbyGetDTO lobbyGetDTO = LobbyDTOMapper.INSTANCE.convertEntityToLobbyGetDTO(joinedLobby);
        lobbyService.removeHandsFromOtherPlayers(lobbyGetDTO, userId);
        lobbyService.transformUsersIntoUsersSlim(lobbyGetDTO, joinedLobby);
        return lobbyGetDTO;
    }

    @DeleteMapping("/lobbies/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void deleteLobby(@PathVariable("lobbyId") Long lobbyId,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        lobbyService.checkIfLobbyExistsById(lobbyId);
        lobbyService.deleteLobby(lobbyId);
    }

    @PutMapping("/lobbies/withdrawal/{lobbyId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void withdrawFromLobby(@PathVariable Long lobbyId, @RequestBody Map<String, Long> requestBody,@RequestHeader("token") String token) 
    {
        userService.isTokenValid(token);
        Long userId = requestBody.get("userId");
        lobbyService.removePlayerFromLobby(lobbyId, userId);
    }
}
