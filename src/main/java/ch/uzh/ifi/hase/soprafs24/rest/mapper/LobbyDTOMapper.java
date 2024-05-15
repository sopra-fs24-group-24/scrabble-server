package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LobbyDTOMapper {

    LobbyDTOMapper INSTANCE = Mappers.getMapper(LobbyDTOMapper.class);

    @Mapping(source = "lobbySize", target = "lobbySize")
    @Mapping(source = "usersInLobby", target = "usersInLobby")
    @Mapping(source = "mode", target = "mode")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "isPrivate", target = "isPrivate")
    Lobby convertLobbyPostDTOtoEntity(LobbyPostDTO lobbyPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "numberOfPlayers", target = "numberOfPlayers")
    @Mapping(source = "lobbySize", target = "lobbySize")
    @Mapping(source = "usersInLobby", target = "usersInLobby")
    @Mapping(source = "gameStarted", target = "gameStarted")
    @Mapping(source = "mode", target ="mode")
    @Mapping(source = "title", target = "title")
    @Mapping(source = "pin", target = "pin")
    //@Mapping(source = "players", target = "players")
    @Mapping(source = "gameOfLobby", target = "gameOfLobby")
    @Mapping(source = "isPrivate", target="isPrivate")
    LobbyGetDTO convertEntityToLobbyGetDTO(Lobby lobby);
}
