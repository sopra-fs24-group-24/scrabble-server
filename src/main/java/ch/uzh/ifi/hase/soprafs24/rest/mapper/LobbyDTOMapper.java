package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LobbyDTOMapper {

    LobbyDTOMapper INSTANCE = Mappers.getMapper(LobbyDTOMapper.class);

    @Mapping(source = "LobbySize", target = "LobbySize")
    @Mapping(source = "UsersInLobby", target = "UsersInLobby")
    Lobby convertLobbyPostDTOtoEntity(LobbyPostDTO lobbyPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "NumberOfPlayers", target = "NumberOfPlayers")
    @Mapping(source = "LobbySize", target = "LobbySize")
    @Mapping(source = "UsersInLobby", target = "UsersInLobby")
    @Mapping(source = "GameStarted", target = "GameStarted")
    LobbyGetDTO convertEntityToLobbyGetDTO(Lobby lobby);
}
